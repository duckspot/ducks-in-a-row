/* dair/src/duckspot/dair/EventsServlet.java
 * 
 * History:
 * 03/17/13 PD
 * 03/25/13 PD changed to use Event.getForm() 
 * 03/31/13 PD - restructure template to use common layout for similar pages
 */
package com.duckspot.diar;

import com.duckspot.diar.model.Event;
import com.duckspot.diar.model.EventDAO;
import com.duckspot.diar.model.Settings;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Display and enter event information within one day.
 */
public class EventServlet extends AbstractServlet {
    
    private DatastoreService datastore;
    private Pattern pathInfoPattern;
    private DateFormat dateEntryFormat;
    
    @Override
    public void init() {
        super.init();
        datastore = DatastoreServiceFactory.getDatastoreService();
        pathInfoPattern = Pattern.compile("/([^/]*)/?(.*)");
        dateEntryFormat = SimpleDateFormat.getDateInstance();
        dateEntryFormat.setLenient(true);
    }
    
    private void parsePathInfo(HttpServletRequest request) {
        
        String pathInfo = request.getPathInfo();
        
        long eventId = -1;
        String displayType = null;
        
        if (pathInfo != null && !pathInfo.isEmpty()) {
            
            Matcher m = pathInfoPattern.matcher(pathInfo);
            if (m.matches()) {
                eventId = Long.parseLong(m.group(1));
                displayType = m.group(2);
            }            
        }
        
        request.setAttribute("displayType", displayType);
        request.setAttribute("id", eventId);        
    }
    
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");
        
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("settings", settings);
        model.put("userBlock", template("layout/userBlock.html", model));
        
        // see if we were called with request attribute error set
        Map<String,String> error = 
                (Map<String,String>)request.getAttribute("error");               
        if (error != null) {
            
            // add top message to errors            
            error.put("top", "correct errors and save again");
            // send errors form submission back to try again
            model.put("error", error);
            model.put("form", (Map<String,String>)request.getAttribute("form"));
            model.put("content", template("eventEdit.html", model));
       
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();        
            out.print(template("layout/twoColumn.html", model));
            return;
        }
        
        // get selectedDate & selectedEventDt from request.pathInfo
        parsePathInfo(request);
        String displayType = (String)request.getAttribute("displayType");
        long eventId = (Long)request.getAttribute("id");
        Event event = new EventDAO(settings).get(eventId);
        if (event == null) {
            response.sendError(404);
            return;
        }
        
        model.put("event", event);
        model.put("form", event.getForm());
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        if (displayType.equals("edit")) {
            model.put("content",template("eventEdit.html", model));
        } 
        else if (displayType.equals("delete")) {
            model.put("content",template("eventDelete.html", model));
        } else {
            model.put("content",template("event.html", model));
        }
        out.print(template("layout/twoColumn.html", model));
    }
    
    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException
    {
        checkSecurity(request);
        
        Settings settings = (Settings)request.getAttribute("settings");                
        
        String action = request.getParameter("action");
        long eventId = Long.parseLong(request.getParameter("id"));
        Event event = null;
        if ("cancel".equals(action)) {
            response.sendRedirect(settings.getURL("event",eventId));
            return;
        } else if ("save".equals(action)) {
            EventDAO eventDAO = new EventDAO(settings);
            event = eventDAO.get(eventId);
            Map <String,String> error = event.submit(request.getParameterMap());
            if (error.isEmpty()) {
                eventDAO.put(event);
            } else {
                request.setAttribute("error", error);
                request.setAttribute("form", request.getParameterMap());
                doGet(request, response);
                return;
            }
        } else if ("delete".equals(action)) {
            EventDAO eventDAO = new EventDAO(settings);
            event = eventDAO.get(eventId);
            event.doDelete(datastore);
        } else if ("start".equals(action)) {
            EventDAO eventDAO = new EventDAO(settings);
            event = eventDAO.get(eventId);
            event.setStartTime(settings.getDtUtil().getThisMinute());
            eventDAO.put(event);
        }
        
        // redirect to events page containing event
        if (event == null || 
            event.getDate().equals(settings.getDtUtil().getToday())) {
            response.sendRedirect(settings.getURL("day"));
        } else {
            response.sendRedirect(settings.getURL("day",event.getDate()));
        }
    }
}