/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.Entity;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Us
 */
public class GoogleCalendar extends EntityWrapper {

    // entity definition
    static final String ENTITY_KIND = "GoogleCalendar";
    
    // keyName is ID
    
    // entity fields
    static final String
              /* String   */ SUMMARY      = "summary";
    static final String
              /* String   */ DESCRIPTION  = "description";
    static final String
              /* String   */ TIME_ZONE_ID = "timeZoneID";
    static final String
              /* String   */ ACCESS_ROLE  = "accessRole";
    
    GoogleCalendar(Entity entity) {
        super(entity);
    }
    
    private static boolean equals(String a, Object b) {
        if (a == null)
            return (b == null);
        else
            return (a.equals(b));
    }
    
    private static String jsonGetStringOrNull(JSONObject json, String key) {
        if (json.has(key)) {
            try {
                return json.getString(key);
            } catch (JSONException ex) {
                throw new Error("unexpected JSON Exception reading "+json, ex);
            }
        } else {
            return null;
        }
    }
    
    boolean load(JSONObject json) {
        
        /* Sample json: calendarListEntry
            "kind": "calendar#calendarListEntry",
            "etag": "\"u8HjymhgSOhDTYSSLBaYUIuJH94/HIft_JbTs2f2GdxK2YnyOn9o9fc\"",
            "id": "en.usa#holiday@group.v.calendar.google.com",
            "summary": "US Holidays",
            "description": "US Holidays",
            "timeZone": "America/Los_Angeles",
            "colorId": "24",
            "backgroundColor": "#a47ae2",
            "foregroundColor": "#000000",
            "selected": true,
            "accessRole": "reader"
         */
        boolean same;        
        same = equals(getSummary(),    
                      jsonGetStringOrNull(json, "summary")) &&
               equals(getDescription(),
                      jsonGetStringOrNull(json, "description")) &&
               equals(getTimeZoneID(), 
                      jsonGetStringOrNull(json, "timeZone")) &&
               equals(getAccessRole(), 
                      jsonGetStringOrNull(json, "accessRole"));
        if (!same) {
            setSummary(    jsonGetStringOrNull(json, "summary"));
            setDescription(jsonGetStringOrNull(json, "description"));
            setTimeZoneID( jsonGetStringOrNull(json, "timeZone"));
            setAccessRole( jsonGetStringOrNull(json, "accessRole"));
            return true;
        }
        return false;        
    }
    
    public String getSummary() {
        return (String)getProperty(SUMMARY);
    }

    public void setSummary(String summary) {
        setProperty(SUMMARY, summary);
    }

    public String getDescription() {
        return (String)getProperty(DESCRIPTION);
    }
    
    public void setDescription(String description) {
        setProperty(DESCRIPTION, description);
    }

    public String getTimeZoneID() {
        return (String)getProperty(TIME_ZONE_ID);
    }

    public void setTimeZoneID(String timeZoneID) {
        setProperty(TIME_ZONE_ID, timeZoneID);
    }

    public String getAccessRole() {
        return (String)getProperty(ACCESS_ROLE);
    }

    public void setAccessRole(String accessRole) {
        setProperty(ACCESS_ROLE, accessRole);
    }
}
