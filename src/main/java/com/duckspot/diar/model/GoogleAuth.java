/* diar/src/com/duckspot/diar/model/GoogleAuthDAO.java
 * 
 * History:
 *  4/ 7/13 PD
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GoogleAuth manages authorization with Google OAuth2 protocol for a single
 * user of ducks-in-a-row.  Only one GoogleAuth object should exist per
 * ducks-in-a-row user.
 * 
 * To perform authorization, 
 * code can getAccessToken() to get a current refreshed token that allows
 * access to Google APIs.  Only one GoogleAuthDAO object exists per 
 * 
 * GoogleAuth is an entity stored in the datastore that holds Google 
 * authorization data for one user.
 * 
 * @author Peter M Dobson
 */
public class GoogleAuth extends AbstractDAO {
    
    // ------------------------------------------------------------------------
    // STATIC CONSTANTS
    
    private static final String client_id 
            = "590071020228.apps.googleusercontent.com";
    private static final String client_secret = "hCZYR_0p4TFyNd8JwK4Wy5gV";

    private static String authURL 
            = "https://accounts.google.com/o/oauth2/auth";
    private static URL tokenURL;
    static {
        try {
            tokenURL = new URL("https://accounts.google.com/o/oauth2/token");
        } catch (MalformedURLException ex) {
            throw new Error("unexpected exception", ex);
        }
    }
    
    // ------------------------------------------------------------------------
    // INSTANCE VARIABLES
    
    private Settings settings;
    private String redirect_uri;
    
    private GoogleAuthData authData; // cache of authData
    
    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    GoogleAuth(Settings settings) {
        super(settings.getKey(), "GoogleAuth");
        this.settings = settings;
    }
    
    // ------------------------------------------------------------------------
    // PROPERTY ACCESSORS FOR TRANSIENT PROPERTIES
    
    /**
     * Before requesting authorization, define the URI of the Servlet that
     * will perform Step 2 of authorization.
     * 
     * @param request 
     */
    public void setRedirectURI(String redirectURI) {
        redirect_uri = redirectURI;
    }
    
    /**
     * Step 1 of authorization, get a URL for the user to visit, that will
     * have Google ask them for authorization, and redirect by to redirectURI.
     * 
     * @return 
     */
    public String getAuthURL(String state) {
        
        String endpoint = "https://accounts.google.com/o/oauth2/auth";
        String response_type = "code";
        String access_type = "offline";
        String scope = getAuth().getGoogleAuthScope().getScopeString();
        String approval_prompt = "force";
        try {
            return String.format("%s?response_type=%s&client_id=%s&"
                    + "redirect_uri=%s&scope=%s&access_type=%s&"
                    + "approval_prompt=%s&state=%s", endpoint,                    
                    URLEncoder.encode(response_type, "UTF-8"),
                    URLEncoder.encode(client_id, "UTF-8"),
                    URLEncoder.encode(redirect_uri, "UTF-8"),
                    URLEncoder.encode(scope, "UTF-8"),
                    URLEncoder.encode(access_type, "UTF-8"),
                    URLEncoder.encode(approval_prompt, "UTF-8"),
                    URLEncoder.encode(state, "UTF-8"));
            
        } catch (UnsupportedEncodingException ex) {
            throw new Error("unexpected exception", ex);
        }
    }
    
    /**
     * Step 2 of authorization, perform a POST to tokenURL to request 
     * authorization tokens.
     * 
     * @param code authorization code returned from initial OAuth request
     * @return GoogleAuth entity wrapper with initial values filled in
     */
    public String requestAuthTokens(String code) {
                
        String grant_type = "authorization_code";
        
        if (redirect_uri == null) {
            throw new Error("must call setRedirectURI() before "
                    + "requestAuthorization()");
        }
        String parameters = "";
        try {
            parameters = String.format("code=%s&client_id=%s&client_secret=%s&"
                    + "redirect_uri=%s&grant_type=%s",
                    URLEncoder.encode(code, "UTF-8"),
                    URLEncoder.encode(client_id, "UTF-8"),
                    URLEncoder.encode(client_secret, "UTF-8"),
                    URLEncoder.encode(redirect_uri, "UTF-8"),
                    URLEncoder.encode(grant_type, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new Error("unexpected exception", ex);
        }
        
        String s = post(tokenURL, parameters);
        JSONObject response;
        try {
            response = new JSONObject(s);
        } catch (JSONException ex) {
            throw new Error("unexpected exception", ex);
        }
        
        if (response.has("error")) {            
            try {                
                return response.getString("error");
            } catch (JSONException ex) {
                throw new Error("unexpected exception", ex);
            }
        }
                
        GoogleAuthData data = getAuth();
        try {
            data.setAccessToken(response.getString("access_token"));
            data.setExpiresIn(response.getInt("expires_in"));
            data.setRefreshToken(response.getString("refresh_token"));
        } catch (JSONException ex) {
            throw new Error("unexpected exception", ex);
        }
        put(data);
        
        settings.getGoogleAuthScope().setScopeString(
                data.getGoogleAuthScope().getScopeString());
        settings.getGoogleAuth().getAuth();
        
        return null;
    }
    
    public void requestRefresh(GoogleAuthData authData) {
        
        String grant_type = "refresh_token";
        
        String parameters = "";
        try {        
            parameters = String.format("client_id=%s&client_secret=%s&"
                    + "refresh_token=%s&grant_type=%s",
                    URLEncoder.encode(client_id, "UTF-8"),
                    URLEncoder.encode(client_secret, "UTF-8"),
                    URLEncoder.encode(authData.getRefreshToken(), "UTF-8"),
                    URLEncoder.encode(grant_type, "UTF-8"));        
        } catch (UnsupportedEncodingException ex) {
            throw new Error("unexpected exception", ex);
        }
        
        JSONObject response;
        try {
            response = new JSONObject(post(tokenURL, parameters));
            
            authData.setAccessToken(response.getString("access_token"));
            authData.setExpiresIn(response.getInt("expires_in"));
        } catch (JSONException ex) {
            throw new Error("unexpected exception", ex);
        }        
    }
    
    // ------------------------------------------------------------------------
    // FETCH
    
    /**
     * Get GoogleAuthData entity wrapper for current user
     * 
     * @return GoogleAuth entity wrapper for current user 
     *         (or null if user never authorized).
     */
    public GoogleAuthData getAuth() {
        
        if (authData == null) {
            PreparedQuery pq = datastore.prepare(ancestorQuery());
            Entity entity = pq.asSingleEntity();
            if (entity != null) {
                authData = new GoogleAuthData(entity);
            } else {
                authData = new GoogleAuthData(newEntity(settings.getEmail()));
            }
        }
        return authData;
    }
    
    public String getAccessToken() {
        
        GoogleAuthData tokens = getAuth();
        if (tokens == null) {
            throw new Error("aurhorization not found in datastore");
        }
        if (!tokens.getExpiresAt().after(new Date())) {
            requestRefresh(tokens);
            put(tokens);
        }
        return tokens.getAccessToken();
    }
    
    // ------------------------------------------------------------------------
    // STATIC METHODS
    
    /**
     * Perform an HTTP POST operation.
     * 
     * @param url
     * @param urlParameters
     * @return 
     */
    
    protected static String post(URL url, String urlParameters) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", ""
                    + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response	
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception ex) {

            throw new Error("unexpected exception", ex);

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }        
}