package org.atlasapi.messaging.workers;

import org.atlasapi.persistence.event.RecentChangeStore;
import org.atlasapi.messaging.EntityUpdatedMessage;

public class RecentChangesLogger extends AbstractWorker {

    private final RecentChangeStore store;

    public RecentChangesLogger(RecentChangeStore store) {
        this.store = store;
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        store.logChange(message);
    }
}