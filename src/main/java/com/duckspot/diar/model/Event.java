/* diar/src/com/duckspot/diar/model/DiarEvent.java
 * 
 * History:
 * 3/22/13 PD if isStarted() then force isStartFixed() true
 * 3/24/13 PD getLengthMinutes() adds to length if event isStarted().
 * 3/24/13 PD getCan
 * 3/24/13 PD getIcon() returns URL for one of three icons to show status of
 *            not started, started, completed
 * 3/25/13 PD new active property, to replace started property, but still 
 *            reads datastore entries that use old started property
 * 3/25/13 PD added getForm() method to return map of initialized edit fields
 * 3/26/13 PD added getStartTimeClass() returns "startFixed" or "startFloating"
 *            to work with style.css to highlight fixed start times differently
 *            from floating start times.
 * 4/ 4/13 PD add URL property to event
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Link;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * DiarEvent represents an event that may happen, is happening, or has happened.
 * 
 * @author Peter M Dobson
 */
public class Event extends EntityWrapper {
    
    // TODO: 9 allow user to configure default length
    private static final int DEFAULT_LENGTH = 15;
    
    private Settings settings;  // user settings
    private DtUtil dtUtil;      // date and time utility
    
    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    
    public Event(Settings settings, Entity entity) {
        super(entity);
        this.settings = settings;
        this.dtUtil = settings.getDtUtil();        
    }
    
    public Event(Settings settings) {
        
        super(new Entity("Event", settings.getKey()));
        this.settings = settings;
        this.dtUtil = settings.getDtUtil();        
        Date today = dtUtil.getToday();
        entity.setProperty("date", today);
        entity.setProperty("description", "");
        entity.setProperty("startFixed", false);
        entity.setProperty("active", false);
        entity.setProperty("completed", false);
        entity.setProperty("startTime", today);        
        entity.setProperty("sequence", new Double(0.0f));
        entity.setProperty("lengthMinutes", DEFAULT_LENGTH);        
    }
    
    // ------------------------------------------------------------------------
    // property accessors for data stored in entity

    public void setDate(Date date) {
        Date cleanDate = dtUtil.stripTime(date);
        if (!cleanDate.equals(date))
            throw new IllegalArgumentException("date must be midnight in "
                    + "users time zone");
        entity.setProperty("date", date);
        if (!isStartFixed()) {
            entity.setProperty("startTime", cleanDate);
        }
    }
    
    public Date getDate() {
        return (Date)getEntity().getProperty("date");
    }
    
    public void setDescription(String description) {        
        setProperty("description",description);
    }
    
    public String getDescription() { 
        return (String) entity.getProperty("description");
    }        
    
    public boolean isStartFixed() {
        return isActive() || isCompleted() || (Boolean)entity.getProperty("startFixed");
    }
    
    public void setStartFixed(boolean value) {
        setProperty("startFixed",value);
    }
    
    /**
     * started items may be active, or may be completed.
     * 
     * @return
     * @deprecated use isActive() instead, as active supports easier query for
     * all currently active items, to stop all currently active items.
     */
    @Deprecated
    public boolean isStarted() {
        Boolean active = (Boolean)entity.getProperty("active");
        if (active != null)
            return active;
        else
            return isCompleted() || (Boolean)entity.getProperty("started");
    }
    
    @Deprecated
    public void setStarted(boolean started) {        
        entity.setProperty("active",started);        
    }
    
    /**
     * only one event may active at a time, it becomes active when we start 
     * working on it, and then becomes inactive and completed when we stop 
     * working on it, or pause and begin working on something else.  
     * completed events may be 'restarted' by creating a new event that 
     * refers to the same task, and then starting that new event.  See restart().
     */
    public void setActive(boolean active)
    {
        entity.setProperty("active",active);
        if (active) {
            setCompleted(false);
        }
        // remove any old started property
        entity.removeProperty("started");
    }
    
    public boolean isActive()
    {        
        Boolean active = (Boolean)entity.getProperty("active");
        
        if (active == null) {
            // upgrade old started property to active property
            active = !isCompleted() && (Boolean)entity.getProperty("started");
            entity.setProperty("active", active);            
        } 
        return active;
    }
    
    /**
     * items that actually occurred in the past are completed.  Items with 
     * future dates may not be completed.  When an item is completed it is no
     * longer 'active'.
     * 
     * @param completed 
     */
    public void setCompleted(boolean completed) {        
        entity.setProperty("completed", completed);
        if (completed) {
            setActive(false);
        }
    }
    
    public boolean isCompleted() {
        return (Boolean) entity.getProperty("completed");
    }
    
    public void setSequence(double sequence) {
        entity.setProperty("sequence", sequence);
    }
   
    public double getSequence() {
        return (Double) entity.getProperty("sequence");        
    }
    
    public void setStartTime(Date time) {
        entity.setProperty("startTime", time);
    }        
    
    public Date getStartTime() {
        return (Date) entity.getProperty("startTime");
    }

    public void setLengthMinutes(int minutes) {
        // TODO: 6 confirm the right way to store an integer property
        // entity.setProperty("lengthMinutes", minutes);
        entity.setProperty("lengthMinutes", minutes);
    }
            
    public int getLengthMinutes() {
        int lengthMinutes = ((Long)entity.getProperty("lengthMinutes")).intValue();
        // if active
        if (isActive()) {
            // calcuate time since start
            Date now = dtUtil.getThisMinute();
            int sinceStart = Util.minutesBetween(now, getStartTime());
            // return longer of sinceStart and lengthMinutes
            return Math.max(sinceStart, lengthMinutes);            
        }
        return lengthMinutes;
    }
    
    public Date getEndTime() {
        return Util.addMinutes(getStartTime(), getLengthMinutes());
    }
    
    public Link getLink() {
        return (Link) entity.getProperty("link");
    }
    
    public void setLink(Link link) {
        entity.setProperty("link", link);
    }
    
    /* ------------------------------------------------------------------------
     * special display features
     */
    
    public String getIcon() {
        
        if (isCompleted()) {
            return "/images/check.png";
        } else {
            if (isActive()) {
                return "/images/cog.gif";
            } else {
                return "/images/box.png";
            }
        }        
    }
    
    public String getStartTimeClass() {
        
        if (isStartFixed()) {
            return "startFixed";
        } else {
            return "startFloating";
        }
    }
    
    /* ------------------------------------------------------------------------
     * actions available
     */
    
    public class ActionsAvailable {
        
        public boolean isStart() {
            return !isActive() && !isCompleted();
        }
        public boolean isStop() {
            return isActive();
        }
        public boolean isReStart() {
            return isCompleted();
        }
        public boolean isCancel() {
            return isActive();
        }
    }        
    
    public ActionsAvailable getCan() {
        return new ActionsAvailable();
    }
    
    public void doStartAt(Date time) {
        setStartTime(time);
        setStartFixed(true);
        setActive(true);        
    }
    
    public void doStop() {
        if (isActive()) {
            // calcuate time since start
            Date now = dtUtil.getThisMinute();
            int sinceStart = Util.minutesBetween(now, getStartTime());
            setLengthMinutes(sinceStart);
            setCompleted(true);
        }
    }
    
    public Event doRestartAt(Date startTime) {        
        if (this.getEndTime().equals(startTime)) {
            // restarting without a pause, just change completed & active            
            setActive(true);
            return this;
        }        
        
        Event newEvent = new Event(settings);
        newEvent.setDescription(getDescription());
        newEvent.setStartTime(startTime);
        newEvent.setStartFixed(true);
        newEvent.setActive(true);
        return newEvent;
    }        
    
    public void doCancel(double sequence) {
        setSequence(sequence);
        setStartFixed(false);
        setActive(false);
    }
    
    public void doDelete(DatastoreService datastore) {
        datastore.delete(getEntity().getKey());
    }
    
    /* ------------------------------------------------------------------------
     * edit form
     */
    
    public Map<String,String> getForm() {
                
        Map<String,String> form = new HashMap<String,String>();
        
        form.put("id", String.format("%d",getId()));
        form.put("date", dtUtil.render(getDate(),"mediumDate"));
        form.put("description", getDescription());
        form.put("length", Util.renderHoursMinutes(getLengthMinutes()));
        form.put("startTime", dtUtil.render(getStartTime(),"shortTime"));
        form.put("sequence", String.valueOf(getSequence()));
        form.put("completed", Util.renderBoolean(isCompleted(),"checked"));
        form.put("active", Util.renderBoolean(isActive(),"checked"));
        form.put("startFixed", Util.renderBoolean(isStartFixed(),"checked"));
        if (getLink() != null) {
            form.put("link", getLink().getValue());
        } else {
            form.put("link", "none");
        }
        
        return form;
    }
    
    private String extract(Map form, String field) {
        
        Object o = form.get(field);
        if (o == null) {
            return null;
        }
        if (o instanceof String[]) {
            String[] sa = (String[])o;
            if (sa.length == 1) {
                return sa[0];
            }
            StringBuilder sb = new StringBuilder();
            String sep = "";            
            for (String s: sa) {
                sb.append(sep);
                sb.append(sa);
                sep = ",";
            }
            return sb.toString();
        }
        System.out.printf("Event: 365: o.getClass().isArray(): %b\n",o.getClass().isArray());
        return o.toString();
    }
    
    public Map<String,String> submit(Map form) {
        
        Map<String,String> error = new HashMap<String,String>();
        String s;
        
        s = extract(form,"date");
        try {
            setDate(dtUtil.parse(s,"mediumDate"));
        } catch (ParseException ex) {
            error.put("date", ex.getLocalizedMessage());
        }
        
        s = extract(form,"description");
        if (s != null) {
            setDescription(s);
        }
        
        s = extract(form,"length");
        if (s != null) {
            int minutes = Util.parseHoursMinutes(s);
            setLengthMinutes(minutes);
        }
        
        s = extract(form,"startTime");
        if (s != null) {
            try {
                setStartTime(dtUtil.parseTime(getDate(), s));
            } catch (ParseException ex) {
                error.put("startTime", ex.getLocalizedMessage());
            }
        }
        
        s = extract(form,"sequence");
        if (s != null) {
            setSequence(Double.parseDouble(s));
        }
        
        s = extract(form,"completed");        
        setCompleted(Util.isTrue(s));
        
        s = extract(form,"active");
        setActive(Util.isTrue(s));
        
        s = extract(form,"startFixed");
        setStartFixed(Util.isTrue(s));
        
        s = extract(form,"link");
        if (s != null) {
            if (!s.isEmpty() && !s.equalsIgnoreCase("none")) {
                setLink(new Link(s));
            } else {
                setLink(null);
            }
        }
        
        return error;
    }
    
    // ------------------------------------------------------------------------
    // debugging help
    
    @Override
    public String toString() {
        if (getDate() == null) {
            return String.format(
                "%s[date:null start:%tr length: %d, desc: %s]", 
                getClass().getName(), getDate(), getStartTime(), 
                getLengthMinutes(), getDescription());
        }
        return String.format(
                "%s[date:%tD start:%tr length: %d, desc: %s]", 
                getClass().getName(), getDate(), getStartTime(), 
                getLengthMinutes(), getDescription());
    }    
}