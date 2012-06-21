package org.atlasapi.messaging.workers;

import org.atlasapi.persistence.event.RecentChangeStore;
import org.atlasapi.persistence.messaging.event.EntityUpdatedEvent;

public class RecentChangesLogger extends AbstractWorker {

    private final RecentChangeStore store;

    public RecentChangesLogger(RecentChangeStore store) {
        this.store = store;
    }

    @Override
    public void process(EntityUpdatedEvent command) {
        store.logChange(command);
    }
}