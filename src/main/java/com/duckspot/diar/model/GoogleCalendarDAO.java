/* diar/src/com/duckspot/diar/model/GoogleCalendarDAO.java
 * 
 * History:
 * 4/12/13 PD
 */
package com.duckspot.diar.model;

import static com.duckspot.diar.model.AbstractDAO.datastore;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Event represents an event that may happen, is happening, or has happened.
 * 
 * @author Peter M Dobson
 */
public class GoogleCalendarDAO extends AbstractDAO {    
    
    GoogleAuth auth;
    GoogleAuthScope scope;    
    
    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    public GoogleCalendarDAO(Settings settings) {
        super(settings.getKey(), GoogleCalendar.ENTITY_KIND);
        auth = settings.getGoogleAuth();
        scope = settings.getGoogleAuthScope();
    }

    // ------------------------------------------------------------------------
    // FILTERS
    
    // ------------------------------------------------------------------------
    // QUERIES
    
    // ------------------------------------------------------------------------
    // ACTIONS
            
    // ------------------------------------------------------------------------
    // FETCH
    
    public GoogleCalendar get(String id)
    {
        try {
            Entity e = datastore.get(createKey(id));
            return new GoogleCalendar(e);
        } catch (EntityNotFoundException ex) {
            return new GoogleCalendar(newEntity(id));
        }
    }

    public GoogleCalendar[] list() {
    
        GoogleCalendar[] result;
        
        if (scope.isCalendarReadWrite() || scope.isCalendarReadOnly()) {
            
            JSONObject json;
            try {
                String request = String.format(
                        "https://www.googleapis.com/calendar/v3/"
                        + "users/me/calendarList?access_token=%s",
                        auth.getAccessToken());
                String response = Http.get(new URL(request));
                json = new JSONObject(response);
                
                JSONArray items = json.getJSONArray("items");
                if (items == null) throw new Error("Failed to get list of calendars.");
                result = new GoogleCalendar[items.length()];
                for (int i=0; i<items.length(); i++) {
                    JSONObject cal = items.getJSONObject(i);
                    String id = cal.getString("id");
                    result[i] = get(id);                    
                    if (result[i].load(cal)) {
                        put(result[i]);
                    }
                }
                return result;
                
            } catch (MalformedURLException ex) {
                throw new Error("unexpected exception", ex);
            } catch (JSONException ex) {
                throw new Error("unexpected exception", ex);
            }                        
        }
        return new GoogleCalendar[0];
    }
}