package org.atlasapi.messaging.workers;

import org.atlasapi.persistence.messaging.event.EntityUpdatedEvent;

public interface RecentChangeStore {

    /**
     * Log a change.
     * @param event
     */
    void logChange(EntityUpdatedEvent event);
    
    /**
     * Logged changes
     * @return
     */
    Iterable<EntityUpdatedEvent> changes();
    
}