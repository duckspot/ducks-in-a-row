/* diar/src/com/duckspot/diar/model/SettingsData.java
 * 
 * History:
 * 3/21/13 PD rewrite parseTime() method
 * 3/22/13 PD change name to Settings
 * 3/22/13 PD change to always exist, even if user not logged in
 * 3/22/13 PD added getURLs() and getURL() functions to build URLs
 * 4/ 1/13 PD define constants for Entity kind & parameter names
 * 4/ 8/13 PD add GoogleAuthScope property
 * TODO: 8 provide admin tool to adjust default GoogleAuthScope
 * TODO: 8 provide admin tool to adjust default TimeZoneID
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Peter M Dobson
 */
public class SettingsData extends EntityWrapper {

    // entity definition
    static final String ENTITY_KIND = "Settings";
    
    // key is email address
    
    // entity fields
    static final String 
              /* boolean  */ USER_REGISTERED = "userRegistered";
    static final String
              /* String   */ NAME = "name";
    static final String
              /* int      */ START_HOUR = "startHour";
    static final int DEFAULT_START_HOUR = 8;
    
    static final String         TIMEZONE_ID = "timeZoneID";
    static final String DEFAULT_TIMEZONE_ID = "US/Pacific";
    
    static final String GOOGLE_AUTH_SCOPE  = "googleAuthScope";
    
    public SettingsData(Entity entity) {
        super(entity);        
    }

    public String getEmail()
    {
        return getKeyName();
    }
        
    public boolean isUserRegistered() {        
        Object o = getProperty(USER_REGISTERED);
        return (o != null) && (Boolean)o;
    }

    public void setUserRegistered(boolean userRegistered) {
        setProperty(USER_REGISTERED, userRegistered);
    }
    
    public String getName() 
    {   
        return (String)getProperty(NAME);
    }

    public void setName(String name)
    {
        setProperty(NAME, name);
    }
    
    public int getStartHour() {
        Object o = getProperty(START_HOUR);
        return (o == null ? DEFAULT_START_HOUR : ((Long)o).intValue());
    }
    
    public void setStartHour(int hour) {
        setProperty(START_HOUR, hour);
    }
    
    public String getTimeZoneID() {
        Object o = getProperty(TIMEZONE_ID);
        return (o == null ? DEFAULT_TIMEZONE_ID : (String)o);
    }
    
    public void setTimeZoneID(String timeZoneID) {
        setProperty(TIMEZONE_ID, timeZoneID);
    }
    
    public GoogleAuthScope getGoogleAuthScope() {
        // this constructor grabs stat using getProperty() and if it's
        // null, sets default value of setAll(true)
        return new GoogleAuthScope(this, GOOGLE_AUTH_SCOPE);
    }
    
    /* ------------------------------------------------------------------------
     * SETTINGS FORM SUPPORT
     */
    
    public Map<String, String> getForm() {

        Map<String, String> form = new HashMap<String, String>();

        form.put("name",            getName());
        form.put("email",           getEmail());
        form.put("startHour",       String.valueOf(getStartHour()));
        form.put("timeZoneID",      getTimeZoneID());
        form.put("calendarChoice",  getGoogleAuthScope().getCalendarChoice());
        
        return form;
    }
    
    public Map<String,String> submitForm(Map<String, String> form) {
        
        Map<String,String> result = new HashMap<String,String>();
        
        if (form.containsKey("name")) {
            setName(form.get("name"));
        }
        if (form.containsKey("email") && 
                !form.get("email").equals(getEmail())) {
            result.put("email", "email address can not be changed");
        }
        if (form.containsKey("startHour")) {
            int hour = 0;
            try {
                hour = Integer.parseInt(form.get("startHour"));
            } catch (NumberFormatException ex) {
                result.put("startHour", "invalid start hour");
            }
            if (hour < 0 || hour > 23) {
                result.put("startHour", "invalid start hour");
            } else {
                setStartHour(hour);
            }            
        }
        if (form.containsKey("timeZoneID")) {
            setTimeZoneID(form.get("timeZoneID"));
        }
        return result;
    }
}
