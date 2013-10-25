/* diar/src/com/duckspot/diar/model/AbstractDAO.java
 * 
 * History:
 * 3/26/13 PD
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * AbstractDAO is a base for building Data Access Objects that perform reads,
 * queries, and updates to the Datastore.
 * 
 * @author Peter M Dobson
 */
public class AbstractDAO {
    
    protected static DatastoreService datastore 
            = DatastoreServiceFactory.getDatastoreService();
    protected Key parentKey;
    protected String entityKind;
    
    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    public AbstractDAO(String entityKind) {
        this.entityKind = entityKind;
    }
            
    public AbstractDAO(Key parent, String entityKind) {
        this.parentKey = parent;
        this.entityKind = entityKind;
    }
            
    protected Entity newEntity() {
        if (parentKey == null) {
            return new Entity(entityKind);
        } else {
            return new Entity(entityKind, parentKey);
        }
    }
    
    protected Entity newEntity(String name) {
        if (parentKey == null) {
            return new Entity(entityKind, name);
        } else {
            return new Entity(entityKind, name, parentKey);
        }
    }
    
    protected Entity newEntity(Long id) {
        if (parentKey == null) {
            return new Entity(entityKind, id);
        } else {
            return new Entity(entityKind, id, parentKey);
        }
    }
    
    public void put(EntityWrapper data) {
        datastore.put(data.getEntity());
    }
    
    protected Query query() {        
        return new Query(entityKind);
    }
    
    protected Query ancestorQuery() {
        if (parentKey == null) {
            throw new Error("ancestorQuery when parent not defined");
        }
        return query().setAncestor(parentKey);
    }    
    
    protected Key createKey(String name)
    {
        if (parentKey == null) {
            return KeyFactory.createKey(entityKind, name);
        } else {
            return KeyFactory.createKey(parentKey, entityKind, name);
        }
    }
    
    protected Key createKey(long id)
    {
        if (parentKey == null) {
            return KeyFactory.createKey(entityKind, id);
        } else {
            return KeyFactory.createKey(parentKey, entityKind, id);
        }
    }

    public PreparedQuery prepare(Query query) {
        return datastore.prepare(query);
    }
    
    static Entity firstIn(PreparedQuery pq) {
        for (Entity e: pq.asIterable()) {
            return e;
        }
        return null;
    }

    // ------------------------------------------------------------------------
}