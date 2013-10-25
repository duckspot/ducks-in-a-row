/* diar/src/com/duckspot/diar/model/GoogleAuthData.java
 * 
 * History:
 *  4/ 9/13 PD
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.Entity;
import java.util.Date;

/**
 * Stores accessToken, refreshToken, and expiresAt, to support GoogleAuth
 * 
 * @author Peter M Dobson
 */
public class GoogleAuthData extends EntityWrapper {

    // entity property names
    static final String GOOGLE_AUTH_SCOPE  = "googleAuthScope";
    static final String ACCESS_TOKEN = "access_token";
    static final String EXPIRES_AT = "expires_at";
    static final String REFRESH_TOKEN = "refresh_token";
    

    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    public GoogleAuthData(Entity entity) {
        super(entity);
    }
    
    // ------------------------------------------------------------------------
    // PROPERTY ACCESSORS FOR PROPERTIES STORED IN ENTITY
    
    public GoogleAuthScope getGoogleAuthScope() {
        // this constructor grabs stat using getProperty() and if it's
        // null, sets default value of setAll(true)
        return new GoogleAuthScope(this, GOOGLE_AUTH_SCOPE);
    }
    
    public void setAccessToken(String access_token) {
        setProperty(ACCESS_TOKEN, access_token);        
    }
    
    public String getAccessToken() {
        return (String)getProperty(ACCESS_TOKEN);
    }
    
    public void setExpiresAt(Date time) {
        setProperty(EXPIRES_AT, time);
    }
    
    public Date getExpiresAt() {
        return (Date)getProperty(EXPIRES_AT);
    }
    
    public void setRefreshToken(String refresh_token) {
        setProperty(REFRESH_TOKEN, refresh_token);
    }
    
    public String getRefreshToken() {
        return (String)getProperty(REFRESH_TOKEN);
    }    
    
    // ------------------------------------------------------------------------
    // SPECIAL PROPERTY SETTERS
    
    public void setExpiresIn(int seconds) {
        
        long ms = System.currentTimeMillis() + seconds*1000L;
        setExpiresAt(new Date(ms));
    }    
}
