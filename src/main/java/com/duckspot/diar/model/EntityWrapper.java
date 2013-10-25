/* diar/src/com/duckspot/diar/model/EntityWrapper
 * 
 * History:
 * 4/ 1/13 PD
 * 4/ 7/13 PD getId() deprecated, remove setEntity()
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public abstract class EntityWrapper {

    protected Entity entity;
    
    protected EntityWrapper(Entity entity) {        
        this.entity = entity;
    }

    Entity getEntity() {
        return entity;
    }
    
    public Key getKey() {        
        return entity.getKey();
    }
    
    public String getKeyName() {
        return getKey().getName();
    }
    
    public long getKeyId() {
        return getKey().getId();
    }
    
    @Deprecated
    public long getId() {
        return getKeyId();
    }

    protected Object getProperty(String name) {
        return entity.getProperty(name);
    }
    
    protected void setProperty(String name, Object value) {
        entity.setProperty(name, value);
    }
}
