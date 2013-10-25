/* diar/src/com/duckspot/diar/model/Settings
 * 
 * History:
 * 3/21/13 PD rewrite parseTime() method
 * 3/22/13 PD change name to Settings
 * 3/22/13 PD change to always exist, even if user not logged in
 * 3/22/13 PD added getURLs() and getURL() functions to build URLs
 * 4/ 1/13 PD define constants for Entity kind & parameter names
 * 4/12/13 PD clean up GoogleServices access
 */
package com.duckspot.diar.model;

import com.duckspot.diar.URLs;
import com.duckspot.diar.google.GoogleServices;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Peter M Dobson
 */
public class Settings extends SettingsData {

    private UserService userService;
    
    private Map<String,String> myURLs;

    // cached values
    private DtUtil dtUtil;
    private GoogleAuth googleAuth;
    private GoogleServices google;
    
    public Settings(Entity entity) {
        
        super(entity);
        
        userService = UserServiceFactory.getUserService();        
        
        myURLs = new HashMap<String, String>();
        myURLs.putAll(URLs.getURLs());        
    }    
        
    public void setRequest(HttpServletRequest request) {
        // login now always returns to register, which then may redirect to home
        myURLs.put("login",  userService.createLoginURL(myURLs.get("register")));
        // logout always returns to home
        myURLs.put("logout", userService.createLoginURL(myURLs.get("home")));
    }    
    
    public boolean isUserLoggedIn() {
        return userService.isUserLoggedIn();
    }
    
    public boolean isUserAdmin() {
        return userService.isUserAdmin();
    }
    
    public TimeZone getTimeZone()
    {        
        return TimeZone.getTimeZone(getTimeZoneID());
    }
    
    public Locale getLocale()
    {        
        // TODO: 8 create logic to allow user to define Locale
        return Locale.getDefault();
    }
    
    /* ------------------------------------------------------------------------
     * CONVENIENCE METHODS
     */
    
    public DtUtil getDtUtil() {
        if (dtUtil == null) {
            dtUtil = DtUtil.getInstance(getLocale(),getTimeZone());
        }
        return dtUtil;
    }
    
    public Date getStartTime(Date date) {
        Calendar cal = getDtUtil().getCalendar();
        Util.clearTime(cal);
        cal.set(Calendar.HOUR_OF_DAY, getStartHour());
        return cal.getTime();
    }
    
    public Map<String,String> getURLs() {
        return myURLs;
    }
    
    public String getURL(String name) {
        if (!myURLs.containsKey(name)) {
            throw new Error("myURLs doesn't contain \""+name+"\"");
        }
        return myURLs.get(name);        
    }
    
    public String getURL(String name, Object...more) {
        StringBuilder sb = new StringBuilder();
        sb.append(myURLs.get(name));
        for (Object o: more) {
            sb.append("/");
            if (o instanceof Date) {
                sb.append(getDtUtil().timeCode((Date)o));
            } else {
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }
    
    public GoogleAuth getGoogleAuth() {
        if (googleAuth == null) {
            googleAuth = new GoogleAuth(this);
        }
        return googleAuth;
    }
    
    public GoogleServices getGoogle() {
        
        if (google == null) {
            google = new GoogleServices(getGoogleAuth());
        }        
        return google;
    }
}
