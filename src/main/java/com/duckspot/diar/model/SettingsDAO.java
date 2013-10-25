/* diar/src/com/duckspot/diar/model/SettingsDAO.java
 * 
 * History:
 * 3/26/13 PD
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * SettingsDAO creates, reads and writes User SettingsData to the Datastore.
 * 
 * @author Peter M Dobson
 */
public class SettingsDAO extends AbstractDAO {
    
    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    public SettingsDAO() {
        super(Settings.ENTITY_KIND);
    }

    // ------------------------------------------------------------------------    
    
    public Settings defaultSettings() {
        Settings result;
        try {            
            result = new Settings(datastore.get(createKey("default")));       
        } catch (EntityNotFoundException ex) {
            result = new Settings(newEntity("default"));
            result.setUserRegistered(false);
            put(result);
        }
        return result;
    }
    
    public Settings anonymousSettings() {
        Settings result;
        try {
            result = new Settings(datastore.get(createKey("anonymous")));
        } catch (EntityNotFoundException ex) {
            result = new Settings(newEntity("anonymous"));
            result.setUserRegistered(false);
            put(result);
        }
        return result;
    }
    
    public Settings currentSettings()
    {
        Settings result;
        UserService userService = UserServiceFactory.getUserService();
        if (userService.isUserLoggedIn()) {
            String email = userService.getCurrentUser().getEmail();
            try {
                result = new Settings(datastore.get(createKey(email)));
            } catch (EntityNotFoundException ex) {
                result = new Settings(newEntity(email));
                result.getEntity().setPropertiesFrom(defaultSettings().getEntity());
            }
        } else {
            result = anonymousSettings();
        }
        return result;
    }
}