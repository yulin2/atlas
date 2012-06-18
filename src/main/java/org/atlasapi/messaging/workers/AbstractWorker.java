package org.atlasapi.messaging.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.atlasapi.persistence.messaging.event.Event;
import org.atlasapi.persistence.messaging.worker.Worker;
import org.atlasapi.serialization.json.JsonFactory;

/**
 * Base {@link org.atlasapi.persistence.messaging.worker.Worker} class providing 
 * {@link org.atlasapi.persistence.messaging.event.Event} unmarshaling and dispatching.
 */
public abstract class AbstractWorker implements Worker {

    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();

    public void onMessage(String message) {
        try {
            Event event = mapper.readValue(message, Event.class);
            event.dispatchTo(this);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
