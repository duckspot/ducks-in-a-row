/* dair/src/com/duckspot/dair/google/GoogleServices.java
 *
 * History:
 *  4/10/13 PD UserInfo
 *  4/11/13 PD getCalendars()
 */
package com.duckspot.diar.google;

import com.duckspot.diar.model.GoogleAuth;
import com.duckspot.diar.model.GoogleAuthScope;
import com.duckspot.diar.model.Http;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Peter M Dobson
 */
public class GoogleServices {
    
    GoogleAuth auth;
    
    public GoogleServices(GoogleAuth auth) {
        this.auth = auth;
    }
    
    public GoogleAuthScope getScope() {        
        return auth.getAuth().getGoogleAuthScope();
    }
    
    public String getAccessToken() {
        return auth.getAccessToken();
    }
    
    public UserInfo getUserInfo() {
        
        if (getScope().isUserinfoProfile()) {
            
            JSONObject json;
            try {
                String request = String.format(
                        "https://www.googleapis.com/oauth2/v1/userinfo"            
                        + "?access_token=%s", getAccessToken());
                String response = Http.get(new URL(request));
                json = new JSONObject(response);
            } catch (MalformedURLException ex) {
                throw new Error("unexpected exception", ex);
            } catch (JSONException ex) {
                throw new Error("unexpected exception", ex);
            }        
            UserInfo result = new UserInfo();
            try {
                result.setName(json.getString("name"));
                result.setLocale(json.getString("locale"));
            } catch (JSONException ex) {
                throw new Error("unexpected exception", ex);
            }
            return result;
        }
        return null;
    }        
}
