/* dair/src/duckspot/google/appengine/AbstractServlet.java
 * 
 * History:
 * 04/09/13 PD - move static URLs data out of model/Settings
 */

package com.duckspot.diar;

import java.util.HashMap;
import java.util.Map;

/**
 * Static URLs map for all URLs except login/logout.  Take care to make 
 * matching changes in web.xml whenever making changes here.
 * 
 * @author Peter Dobson
 */
public class URLs {
    private static final Map<String,String> URLs = new HashMap<String,String>();
    static {
        
        // home: HomeServlet
        // displays welcome page, or forwards to ready
        URLs.put("home",           "/");                
        
        // registerGoogleAuth: RegisterGoogleAuthServlet
        // ask user what google services to authorize, and begins authorization
        URLs.put("register",       "/register");
        
        // googleAuthCallback: GoogleAuthCallbackServlet
        // completes google authorization and forwards to registerSettings
        URLs.put("googleCallback", "/googleCallback");
        
        // registerSettings: SettingServlet(content=registerSettings.html)
        // confirm Name and Timezone and set userRegistered flag
        URLs.put("registerSettings", "/register2");
        
        // settings: SettingServlet
        // allow modification of Name and Timezone and set userRegistered flag
        URLs.put("settings",       "/settings");        
        
        // security: SecurityServlet
        // display security error message
        URLs.put("security",       "/security");        
        
        // day: DayServlet
        // display events on one day
        URLs.put("day",            "/day");        
        
        // day: DayServlet
        // display events on one day
        URLs.put("ready",          "/day");
        
        // event: EventServlet
        // show/edit details of an event
        URLs.put("event",          "/event");
    }
    public static Map<String,String> getURLs() {
        return URLs;
    }
}
