/* diar/src/com/duckspot/diar/model/GoogleAuthScope.java
 * 
 * History:
 *  4/ 9/13 PD
 *  4/12/13 PD added getForm() and submitForm() methods
 *  4/12/13 PD added setScopeString() method to allow copying scope
 */
package com.duckspot.diar.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manage set of Google OAuth scopes for GoogleAuthData.
 * 
 * @author Peter M Dobson
 */
public class GoogleAuthScope {

    private static final String USERINFO_PROFILE 
            = "https://www.googleapis.com/auth/userinfo.profile";
    private static final String CALENDAR_READWRITE 
            = "https://www.googleapis.com/auth/calendar";    
    private static final String CALENDAR_READONLY 
            = "https://www.googleapis.com/auth/calendar.readonly";
    

    private EntityWrapper data;
    private String propertyName;
    private String scopeString;
    private Set<String> scopeSet;
    
    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    public GoogleAuthScope(EntityWrapper data, String propertyName) {
        this.data = data;
        this.propertyName = propertyName;
        scopeString = (String)data.getProperty(propertyName);
        
        // if not defined, default to all scopes true
        if (scopeString == null) {
            setAll(true);
        }
    }
    
    // ------------------------------------------------------------------------
    // updateData
    
    private void initSet() {
        if (scopeSet == null) {
            // chose ArrayList instead of HashSet as number of elements is very small
            scopeSet = new HashSet<String>();
            if (scopeString != null) {
                for (String scope: scopeString.split(" ")) {
                    scopeSet.add(scope);
                }
            }
        }
    }
    
    private void updateData() {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String scope: scopeSet) {
            sb.append(sep).append(scope);
            sep = " ";
        }
        setScopeString(sb.toString());        
    }
    
    // ------------------------------------------------------------------------
    // PROPERTY ACCESSORS
    
    public void setUserinfoProfile(boolean set) {
        initSet();
        if (set) {
            scopeSet.add(USERINFO_PROFILE);
        } else {
            scopeSet.remove(USERINFO_PROFILE);            
        }
        updateData();
    }
    
    public boolean isUserinfoProfile() {
        initSet();
        return scopeSet.contains(USERINFO_PROFILE);
    }
    
    public void setCalendarReadWrite(boolean set) {
        initSet();
        if (set) {
            scopeSet.add(CALENDAR_READWRITE);
        } else {
            scopeSet.remove(CALENDAR_READWRITE);            
        }
        updateData();
    }
    
    public boolean isCalendarReadWrite() {
        initSet();
        return scopeSet.contains(CALENDAR_READWRITE);       
    }
    
    public void setCalendarReadOnly(boolean set) {
        initSet();
        if (set) {
            scopeSet.add(CALENDAR_READONLY);
        } else {
            scopeSet.remove(CALENDAR_READONLY);
        }
        updateData();
    }
    
    public boolean isCalendarReadOnly() {
        initSet();
        return scopeSet.contains(CALENDAR_READONLY);
    }
    
    public String getCalendarChoice() {
        if (isCalendarReadWrite()) {
            return "read/write";
        } 
        else if (isCalendarReadOnly()) {
            return "read-only";
        }
        else {
            return "none";
        }
    }
    
    public void setCalendarChoice(String choice) {
        if ("read/write".equalsIgnoreCase(choice)) {
            setCalendarReadWrite(true);
            setCalendarReadOnly(false);
        } else if ("read-only".equalsIgnoreCase(choice)) {
            setCalendarReadWrite(false);
            setCalendarReadOnly(true);
        } else {
            setCalendarReadWrite(false);
            setCalendarReadOnly(false);
        }
    }
    
    @Deprecated
    public void setCalendarReadonly(boolean set) {
        setCalendarReadOnly(set);
    }
    
    @Deprecated
    public boolean isCalendarReadonly() {        
        return isCalendarReadOnly();
    }
    
    @Deprecated
    public void setCalendar(boolean set) {
        setCalendarReadWrite(set);
    }
    
    @Deprecated
    public boolean isCalendar() {        
        return isCalendarReadWrite();
    }
    
    public final void setAll(boolean set) {
        initSet();
        if (set) {
            scopeSet.add(USERINFO_PROFILE);
            scopeSet.add(CALENDAR_READWRITE);
            scopeSet.add(CALENDAR_READONLY);
        } else {
            scopeSet.clear();
        }
        updateData();
    }
    
    public void setScopeString(String scopeString) {
        this.scopeString = scopeString;
        data.setProperty(propertyName, scopeString);
    }
    
    public String getScopeString() {
        return scopeString;
    }
    
    /* ------------------------------------------------------------------------
     * SETTINGS FORM SUPPORT
     */
    
    public Map<String, String> getForm() {

        Map<String, String> form = new HashMap<String, String>();

        form.put("userinfoProfile", Boolean.toString(isUserinfoProfile()));
        form.put("calendarChoice",  getCalendarChoice());
        
        return form;
    }
    
    public Map<String,String> submitForm(Map<String, String> form) {
        
        Map<String,String> result = new HashMap<String,String>();
        
        if (form.containsKey("userinfoProfile")) {
            setUserinfoProfile(true);
        } else {
            setUserinfoProfile(false);
        }
        
        if (form.containsKey("calendarChoice")) {
            setCalendarChoice(form.get("calendarChoice"));
        }

        return result;
    }
    
    public String toString() {
        initSet();
        return scopeSet.toString();
    }
}
