/*
 * History:
 * 3/24/13 PD - In addFloatingEvent() correct so items started in correct 
 *              order, and no events start at same time.
 * 4/ 5/13 PD - refactor "currentEvent" name to "selectedEvent" throughout code
 */
package com.duckspot.diar.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DiarDay represents a day (today, past or future) and the events that have 
 * occurred, are scheduled for, or might occur during that day.
 * 
 * It's generally organized as a list of DairEvent.  The events are all read
 * as a block beginning with working on the day, and are written bit by bit
 * as they are updated or modified.
 */

public class Day {
    
    private Settings settings;
    private DtUtil dtUtil;
    private Date selectedDate;
    
    private boolean today;
    
    // all events today
    private List<Event> events;    
    
    // selected event
    private Date selectedEventTime;
    private long selectedEventId;    
    private Event selectedEvent;
    
    /** 
     * events before selected event
     */
    private List<Event> eventsBefore = null;
    /** 
     * events after selected event
     */
    private List<Event> eventsAfter = null;
    
    public Day(Settings settings, Date selectedDate) {
        
        this.settings = settings;
        this.dtUtil = settings.getDtUtil();
        this.selectedDate = selectedDate;
        
        Date dateToday = dtUtil.getToday();
        today = dateToday.equals(selectedDate);        
            
        events = new ArrayList<Event>();
        
        selectedEventId = -1;            
    }

    public Date getSelectedDate() {
        return selectedDate;
    }
    
    public void addFloatingEvent(Event newEvent) {
    
        Date dayStartTime = settings.getStartTime(selectedDate);
        Date previousEnd = dayStartTime;                        
        Date earliestStart;
        if (today) {
            // today we can't start a floating event before this minute
            earliestStart = dtUtil.getThisMinute();
        } else {
            // other days we can start at the first hour of the day
            earliestStart = dayStartTime;
        }                
        
        int i = 0;
        // skip forward past events that start before earliestStart
        for ( ; i<events.size(); i++) {
            previousEnd = events.get(i).getEndTime();
            if (!events.get(i).getStartTime().before(earliestStart)) {
                break;
            }
        }
       
        // skip forward past spaces that are too short to hold newEvent        
        for ( ;i<events.size(); i++) {
            
            // determine minutes between events            
            int minutes = Util.minutesBetween(events.get(i).getStartTime(),
                                              previousEnd);
            previousEnd = events.get(i).getEndTime();
            
            // exit loop when we find enough minutes between to hold newEvent
            if (minutes >= newEvent.getLengthMinutes()) {
                break;
            }
        }
        
        // either i points to item to insert before
        // or i == events.size()
        
        // start after later of previousEnd and earliestStart
        Date startTime = previousEnd.after(earliestStart) ? previousEnd 
                                                          : earliestStart;
        newEvent.setStartTime(startTime);        
        
        // add event
        events.add(i, newEvent);  
    }
    
    public void addFixedEvent(Event event) {
        events.add(event);
    }
    
    public void setSelectedTime(Date time) {
        this.selectedEventTime = time;
    }
    
    public void setSelectedEventId(long eventId) {
        this.selectedEventId = eventId;
    }
    
    /**
     * return selected time (or midnight of selected date)
     * @return 
     */
    public Date getSelectedTime() {
        if (selectedEventTime != null) {
            return selectedEventTime;
        }
        return selectedDate;
    }
    
    private void separateEvents() {
        boolean before = true;
        boolean after = false;
        boolean foundSelectedEvent;
        eventsBefore = new ArrayList<Event>();        
        for (Event event: events) {
            if (after) {
                eventsAfter.add(event);
            } else {
                foundSelectedEvent = 
                        selectedEventTime != null 
                        && event.getEndTime().after(selectedEventTime);
                if (!foundSelectedEvent && selectedEventId > 0 
                        && event instanceof Event) {
                    foundSelectedEvent = ((Event)event).getId() == selectedEventId;
                }
                if (foundSelectedEvent) {
                        after = true;
                        before = false;
                        selectedEvent = event;
                        eventsAfter = new ArrayList<Event>();
                }
            }
            if (before) {
                eventsBefore.add(event);
            }
        }
    }
    
    public List<Event> getEventsBefore() {
        if (eventsBefore == null) {
            separateEvents();
        }        
        return eventsBefore;
    }
    
    public List<Event> getEventsAfter() {        
        if (eventsAfter == null) {
            separateEvents();
        }
        return eventsAfter;
    }
    
    public Event getSelectedEvent() {
        if (eventsBefore == null) {
            separateEvents();
        }
        return selectedEvent;
    }
    
    public String getCalendar1() {
        Calendar cal = dtUtil.getCalendar();
        cal.setTime(selectedDate);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (cal.get(Calendar.DAY_OF_MONTH) <= 15) {
            cal.add(Calendar.MONTH, -1);
        }
        cal.set(Calendar.DAY_OF_MONTH,1);
        return dtUtil.getNavCal(settings.getURL("day"),cal);
    }
    
    public String getCalendar2() {
        Calendar cal = dtUtil.getCalendar();
        cal.setTime(selectedDate);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (cal.get(Calendar.DAY_OF_MONTH) > 15) {
            cal.add(Calendar.MONTH, 1);
        }
        cal.set(Calendar.DAY_OF_MONTH,1);
        return dtUtil.getNavCal(settings.getURL("day"),cal);
    }
    
    /* ------------------------------------------------------------------------
     * SCRAP
     */
    
    /*
    private static Date addMinutes(Date d, int minutes) {
        
        long ms = minutes * 60L * 1000L;
        return new Date(d.getTime() + ms);
    }
    
    private int minuteInDay(Date d) {
        
        long ms = d.getTime() - date.getTime();
        return (int)(ms / 60L / 1000L);
    }
        
    private int findTime(Date time) {
        
        int i;
        for (i=0; i<events.size(); i++) {
            Event event = events.get(i);
            if (event.isEmpty() && event.getSoftEndTime().after(time)) {
                return i;
            }
        }
        // this should never occur unless there's more than 4000 years of
        // work to do in one day
        throw new Error("Can't find time.");
    }
    
    private int findTime(int minutes) {
        int i;
        for (i=0; i<events.size(); i++) {
            boolean isLast = (i == events.size()-1);
            Event event = events.get(i);
            if ( event.isEmpty() 
                    && (event.getLengthMinutes() > minutes || isLast) ) {
                return i;
            }
        }
        // this should never occur unless there's more than 4000 years of
        // work to do in one day
        throw new Error("Can't find time.");
    }
    */
    
    /**
     * carves out time in first possible EmptyEvent in events
     * 
     * @param time
     * @param lengthMinutes
     * @return index of an EmptyEvent with time and lengthMinutes requested
     */    
    /*
    private int makeTime(Date time, int lengthMinutes) {
        
        System.out.printf("DiarDay(104): time: %tc\n", time);
        System.out.printf("DiarDay(105): lengthMinutes: %d\n", lengthMinutes);
        
        Date endTime = addMinutes(time, lengthMinutes);
        System.out.printf("DiarDay(108): endTime: %tc\n", time);
        int i = findTime(time);
        System.out.printf("DiarDay(110): i: %d\n", time);
        if (i<0)
            return i;
        
        Event oldEmpty = (EmptyEvent)events.get(i);        
        
        Event newEmpty;
        int min;        
        
        // newEmpty for minutes before time
        min = minutesBetween(oldEmpty.getSoftStartTime(), time);        
        if (min > 0) {
            newEmpty = new EmptyEvent(oldEmpty.getSoftStartTime(),min);
            events.add(i, newEmpty);
            i++;
            oldEmpty.setLengthMinutes(oldEmpty.getLengthMinutes() - min);
        }
        
        // newEmpty for minutes after time+length
        min = minutesBetween(endTime, oldEmpty.getSoftEndTime());
        if (min > 0) {
            newEmpty = new EmptyEvent(endTime, min);
            events.add(i+1, newEmpty);
            oldEmpty.setLengthMinutes(lengthMinutes);
        }
        return i;
    }
    */
    /**
     * carves out time in first possible EmptyEvent in events
     * 
     * @param time
     * @param lengthMinutes
     * @return index of an EmptyEvent with time and lengthMinutes requested
     */
    /*
    private int makeTime(int minutes) {
        
        int i = findTime(minutes);
        if (i<0)
            return i;
        
        Event oldEmpty = (EmptyEvent)events.get(i);        
        
        Event newEmpty;
        int min;        
        
        // newEmpty for remaining minutes
        min = oldEmpty.getLengthMinutes() - minutes;
        if (min > 0) {
            newEmpty = new EmptyEvent(oldEmpty.getSoftStartTime(), min);
            events.add(i+1, newEmpty);            
            oldEmpty.setLengthMinutes(oldEmpty.getLengthMinutes() - min);
        }
        
        return i;
    }
    
    private void dumpEvents() {
        System.out.printf("DiarDay(163): event.size(): %s\n", events.size());
        for (Event event: events) {
            System.out.printf("DiarDay(165): event: %s\n", event);
        }
    }
    */    
}