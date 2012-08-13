package org.atlasapi.messaging.workers;

import org.atlasapi.persistence.messaging.MessageStore;
import org.atlasapi.messaging.EntityUpdatedMessage;

public class MessageLogger extends AbstractWorker {

    private final MessageStore store;

    public MessageLogger(MessageStore store) {
        this.store = store;
    }

    @Override
    public void process(EntityUpdatedMessage message) {
        store.add(message);
    }
}