/* dair/src/duckspot/dair/DayServlet.java
 * 
 * History:
 * 03/13/13 PD - remove response.encodeURL & response.encodeRedirectURL calls 
 *               because we no longer use sessions.
 *  3/14/13 PD - use settings.queryEvents() to query events
 *  3/14/13 PD - change import statements to not use wildcards
 *  3/17/13 PD - start button
 *  3/22/13 PD - clean up to match new cleaner Event (no soft start, or empty)
 *  3/24/13 PD - action buttons change using ${if selectedEvent.can.action}
 *  3/31/13 PD - restructure template to use common layout for similar pages
 *  4/ 4/13 PD - fix start after last, restart after last fixed because of fix
 *               of EventDAO.lastEndTime()
 *  4/ 4/13 PD - fix cancel button (by fixed EventDAO.minSequence())
 */
package com.duckspot.diar;

import com.duckspot.diar.model.Day;
import com.duckspot.diar.model.DtUtil;
import com.duckspot.diar.model.Event;
import com.duckspot.diar.model.EventDAO;
import com.duckspot.diar.model.Settings;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Display and enter event information within one day.
 */
public class DayServlet extends AbstractServlet {

    private static DateFormat 
            urlDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    private static Pattern 
            urlDatePattern = Pattern.compile("^/\\d{4}\\.\\d{2}\\.\\d{2}");
    private static DateFormat 
            urlDateTimeFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm");
    private static Pattern 
            urlDateTimePattern = Pattern.compile("^/\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}$");
    private static Pattern 
            urlEventIdPattern = Pattern.compile("^/\\d+$");
    
    private void parsePathInfo(HttpServletRequest request) {

        Settings settings = (Settings) request.getAttribute("settings");
        DtUtil dtUtil = settings.getDtUtil();

        String pathInfo = request.getPathInfo();

        Date selectedDate = null;
        Date selectedTime = null;
        String selectedEventId = null;
        
        if (pathInfo != null && !pathInfo.isEmpty()) {

            // get urlDateTime & urlDate values
            try {
                if (urlDateTimePattern.matcher(pathInfo).matches()) {
                    selectedTime = urlDateTimeFormat.parse(
                            pathInfo.substring(1));
                } 
                else if (urlDatePattern.matcher(pathInfo).matches()) {
                    selectedDate = urlDateFormat.parse(pathInfo.substring(1));
                } 
                else if (urlEventIdPattern.matcher(pathInfo).matches()) {
                    selectedEventId = pathInfo.substring(1);
                    EventDAO eventDAO = new EventDAO(settings);
                    Event selectedEvent = eventDAO.get(
                            Long.parseLong(selectedEventId));
                    if (selectedEvent != null) {
                        selectedDate = selectedEvent.getDate();
                        selectedTime = selectedEvent.getStartTime();
                    }
                }
            } catch (ParseException ex) {
                throw new Error("Unexpected exception", ex);
            }
        }
        if (selectedDate == null) {
            selectedDate = dtUtil.getToday();
        }
        if (selectedTime == null) {
            selectedTime = dtUtil.getThisMinute();
        }
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedTime", selectedTime);
        request.setAttribute("selectedEventId", selectedEventId);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        checkSecurity(request);        
        Settings settings = (Settings) request.getAttribute("settings");
        DtUtil dtUtil = settings.getDtUtil();

        // get selectedDate & selectedEventDt from request.pathInfo
        parsePathInfo(request);
        Date selectedDate = (Date) request.getAttribute("selectedDate");
        Date selectedTime = (Date) request.getAttribute("selectedTime");
        String selectedEventId = (String) request.getAttribute("selectedEventId");
        
        Day day = new Day(settings, selectedDate);
        if (selectedEventId != null) {
            day.setSelectedEventId(Long.parseLong(selectedEventId));
        } else {
            day.setSelectedTime(selectedTime);
        }
        // query events and put them into day
        EventDAO eventDAO = new EventDAO(settings);
        PreparedQuery pq;
        // first the fixed events
        pq = eventDAO.prepare(eventDAO.dayFixedQuery(selectedDate));
        for (Entity eventEntity: pq.asIterable()) {
            Event event = new Event(settings, eventEntity);
System.out.printf("DayServlet: 129: event.getDescription(): %s\n",event.getDescription());
            day.addFixedEvent(event);
        }
        // then the floating events
        pq = eventDAO.prepare(eventDAO.dayFloatingQuery(selectedDate));
        for (Entity eventEntity: pq.asIterable()) {
            Event event = new Event(settings, eventEntity);
System.out.printf("DayServlet: 136: event.getDescription(): %s\n",event.getDescription());
            day.addFloatingEvent(event);
        }
        
        Event selectedEvent = day.getSelectedEvent();
        if (selectedEvent != null) {
            selectedEventId = String.valueOf(((Event) selectedEvent).getId());
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("settings", settings);
        model.put("userBlock", template("layout/userBlock.html", model));
        model.put("day", day);
        model.put("displayDate", dtUtil.render(selectedDate,"prefixDate"));
        // TODO: 4 only display time if it's TODAY
        model.put("time", dtUtil.getThisMinute());
        // TODO: 5 remove calendar1 & calendar2 from model (access is through day)
        model.put("calendar1", day.getCalendar1());
        model.put("calendar2", day.getCalendar2());
        model.put("selectedEvent", selectedEvent);
        model.put("selectedEventId", selectedEventId);
        model.put("eventsBefore", day.getEventsBefore());
        model.put("eventCurrent", selectedEvent);
        model.put("eventsAfter", day.getEventsAfter());
        model.put("content", template("events.html", model));
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(template("layout/twoColumn.html", model));
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        checkSecurity(request);

        // settings
        Settings settings = (Settings) request.getAttribute("settings");
        DtUtil dtUtil = settings.getDtUtil();

        // get selectedDate & selectedEventDt from request.pathInfo
        parsePathInfo(request);
        Date selectedDate = (Date) request.getAttribute("selectedDate");
        Date selectedEventDt = (Date) request.getAttribute("selectedEventDt");

        String action = request.getParameter("action");
        if ("new event".equals(action)) {

            // new event
            EventDAO eventDAO = new EventDAO(settings);
            Event newEvent = new Event(settings);
            // TODO: 5 newEvent.placeBefore(selectedEventDt);
            newEvent.setStartTime(selectedEventDt);
            newEvent.setDescription(request.getParameter("description"));
            newEvent.setDate(selectedDate);
            eventDAO.put(newEvent);

            // redirect to events page containing new event            
            response.sendRedirect(
                    settings.getURL("day", newEvent.getStartTime()));
            return;
        } else if ("details".equals(action)) {
            response.sendRedirect(settings.getURL("event",
                    request.getParameter("eventId")));
            return;
        } else if ("edit".equals(action)) {
            response.sendRedirect(settings.getURL("event",
                    request.getParameter("eventId"),
                    "edit"));
            return;
        } else if ("start now".equals(action)) {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            Event event = new EventDAO(settings).get(eventId);
            Date date = event.getDate();
            if (!date.equals(dtUtil.getToday())) {
                throw new Error("attempt to start event that is not today");
            }
            EventDAO eventDAO = new EventDAO(settings);
            eventDAO.doStopOthers(event.getKeyId());
            event.doStartAt(dtUtil.getThisMinute());
            eventDAO.put(event);
            response.sendRedirect(settings.getURL("day",
                    event.getStartTime()));
            return;
        } else if ("start after last".equals(action)) {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            EventDAO eventDAO = new EventDAO(settings);
            Event event = eventDAO.get(eventId);            
            Date lastEndTime = eventDAO.lastEndTime();
            event.doStartAt(lastEndTime);
            eventDAO.put(event);
            response.sendRedirect(settings.getURL("day", event.getStartTime()));
            return;
        } else if ("restart now".equals(action)) {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            EventDAO eventDAO = new EventDAO(settings);
            Event event = eventDAO.get(eventId);
            Event newEvent = event.doRestartAt(dtUtil.getThisMinute());
            eventDAO.put(newEvent);
            response.sendRedirect(
                    settings.getURL("day", newEvent.getStartTime()));
            return;
        } else if ("restart after last".equals(action)) {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            EventDAO eventDAO = new EventDAO(settings);
            Event event = eventDAO.get(eventId);
            Date lastEndTime = eventDAO.lastEndTime();
            Event newEvent = event.doRestartAt(lastEndTime);
            eventDAO.put(newEvent);
            response.sendRedirect(
                    settings.getURL("day", newEvent.getStartTime()));
            return;
        } else if ("stop".equals(action)) {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            EventDAO eventDAO = new EventDAO(settings);
            Event event = eventDAO.get(eventId);
            event.doStop();
            eventDAO.put(event);
            response.sendRedirect(settings.getURL("day", event.getStartTime()));
            return;
        } else if ("cancel".equals(action)) {
            EventDAO eventDAO = new EventDAO(settings);            
            double beforeFirstFloating = eventDAO.minSequence() - 1.0f;
            long eventId = Long.parseLong(request.getParameter("eventId"));
            Event event = eventDAO.get(eventId);
            event.doCancel(beforeFirstFloating);
            eventDAO.put(event);
            response.sendRedirect(settings.getURL("day", event.getStartTime()));
            return;
        } else if ("delete".equals(action)) {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            response.sendRedirect(settings.getURL("event", eventId, "delete")); 
            return;
        }

        // redirect to events page containing event
        if (selectedDate.equals(dtUtil.getToday())) {
            response.sendRedirect(settings.getURL("day"));
        } else {
            response.sendRedirect(settings.getURL("day", selectedDate));
        }
    }
}