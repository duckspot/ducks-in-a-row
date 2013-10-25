/* diar/src/com/duckspot/diar/model/EventDAO.java
 * 
 * History:
 * 3/22/13 PD if isStarted() then force isStartFixed() true
 * 3/24/13 PD getLengthMinutes() adds to length if event isStarted().
 * 3/24/13 PD getCan
 * 3/24/13 PD getIcon() returns URL for one of three icons to show status of
 *            not started, started, completed
 * 3/25/13 PD new active property, to replace started property, but still 
 *            reads datastore entries that use old started property
 * 3/25/13 PD added getForm() method to return map of initialized edit fields
 * 3/26/13 PD added minSequence() returns lowest sequence for todays unstarted 
 *            floating events.
 * 4/ 4/13 PD fix activeEvent() to return null if no event is active, which in
 *            turn fixes lastEndTime() to return correct value.
 * 4/ 4/13 PD fix minSequence() to handle no items found by query.
 * 4/ 7/13 PD revise to use AbstractDAO
 */
package com.duckspot.diar.model;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.util.Date;

/**
 * Event represents an event that may happen, is happening, or has happened.
 * 
 * @author Peter M Dobson
 */
public class EventDAO extends AbstractDAO {
    
    private Settings settings;
    
    // ------------------------------------------------------------------------
    // CONSTRUCTOR
    
    public EventDAO(Settings settings) {
        super(settings.getKey(), "Event");
        this.settings = settings;
    }

    // ------------------------------------------------------------------------
    // FILTERS
    
    private Filter activeFilter() {
        return new FilterPredicate("active", FilterOperator.EQUAL, true);
    }
    private Filter startedFilter() {
        return new FilterPredicate("started", FilterOperator.EQUAL, true);
    }
    private Filter startFixedFilter() {
        return new FilterPredicate("startFixed", FilterOperator.EQUAL, true);
    }    
    private Filter notStartFixedFilter() {
        return new FilterPredicate("startFixed", FilterOperator.EQUAL, false);
    }
    private Filter completedFilter() {
        return new FilterPredicate("completed", FilterOperator.EQUAL, true);
    }    
    private Filter notCompletedFilter() {
        return new FilterPredicate("completed", FilterOperator.EQUAL, false);
    }   
    private Filter dateFilter(Date date) {
        return new FilterPredicate("date", FilterOperator.EQUAL, date);
    }
    private Filter todayFilter() {
        return dateFilter(settings.getDtUtil().getToday());
    }
    
    // ------------------------------------------------------------------------
    // QUERIES
    
    private Query oldActiveQuery() {                
        return ancestorQuery().setFilter(                
                CompositeFilterOperator.and(startedFilter(),
                                            notCompletedFilter()));
    }
    private Query activeQuery() {        
        return ancestorQuery().setFilter(activeFilter());
    }
    
    private Query completedTodayBackwardsQuery() {        
        return ancestorQuery().setFilter(
                CompositeFilterOperator.and(todayFilter(),
                                            completedFilter()))
                .addSort("startTime", Query.SortDirection.DESCENDING);
    }
    
    private Query notCompletedNotStartFixedTodayQuery() {
        return ancestorQuery().setFilter(
                CompositeFilterOperator.and(notCompletedFilter(),
                                            notStartFixedFilter(),
                                            todayFilter()))
                .addSort("sequence", Query.SortDirection.ASCENDING);
    }
    
    public Query dayFixedQuery(Date date) {
        return ancestorQuery().setFilter(
                CompositeFilterOperator.and(dateFilter(date),
                                            startFixedFilter()))
                .addSort("startTime",Query.SortDirection.ASCENDING)
                .addSort("sequence",Query.SortDirection.ASCENDING);
    }
    
    public Query dayFloatingQuery(Date date) {
        return ancestorQuery().setFilter(
                CompositeFilterOperator.and(dateFilter(date),
                                            notStartFixedFilter()))                
                .addSort("sequence",Query.SortDirection.ASCENDING);
    }
    
    // ------------------------------------------------------------------------
    // ACTIONS
    
    private void doOldStopOthers(long id) {
                
        PreparedQuery pq = datastore.prepare(oldActiveQuery());
        for (Entity e: pq.asIterable()) {
            if (e.getKey().getId() == id) {
                break;
            }
            Event event = new Event(settings, e);
            event.doStop();
            datastore.put(event.getEntity());
        }
    }
    
    public void doStopOthers(long id) {

        doOldStopOthers(id);
                
        PreparedQuery pq = datastore.prepare(activeQuery());
        for (Entity e: pq.asIterable()) {
            if (e.getKey().getId() == id) {
                break;
            }
            Event event = new Event(settings, e);
            event.doStop();
            datastore.put(event.getEntity());
        }
    }

    // ------------------------------------------------------------------------
    // FETCH
    
    public Event get(long id)
    {
        try {        
            return new Event(settings, datastore.get(createKey(id)));
        } catch (EntityNotFoundException ex) {
            return null;
        }
    }
    
    public Event activeEvent()
    {    
        PreparedQuery pq = datastore.prepare(activeQuery());
        Entity entity = pq.asSingleEntity();
        if (entity == null) {
            return null;
        }
        return new Event(settings, entity);
    }
    
    /**
     * @return return current time if an event is currently active, or end time of 
     * last completed item today
     */
    public Date lastEndTime() {
        
        Event e = activeEvent();
        if (e != null) {
            return settings.getDtUtil().getThisMinute();
        } else {
            PreparedQuery pq = 
                    datastore.prepare(completedTodayBackwardsQuery());
            Event lastCompleted = new Event(settings, firstIn(pq));
            return lastCompleted.getEndTime();
        }
    }

    public double minSequence() {
        PreparedQuery pq = 
                datastore.prepare(notCompletedNotStartFixedTodayQuery());
        Entity e = firstIn(pq);
        if (e == null) {
            return 1.0;
        }
        Event firstIncompleteFloating = new Event(settings, firstIn(pq));
        return firstIncompleteFloating.getSequence();
    }
}