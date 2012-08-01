package org.atlasapi.messaging.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.atlasapi.messaging.Message;
import org.atlasapi.messaging.worker.Worker;
import org.atlasapi.serialization.json.JsonFactory;

/**
 * Base {@link org.atlasapi.persistence.messaging.worker.Worker} class providing 
 * {@link org.atlasapi.persistence.messaging.Message} unmarshaling and dispatching.
 */
public abstract class AbstractWorker implements Worker {

    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();

    public void onMessage(String message) {
        try {
            Message event = mapper.readValue(message, Message.class);
            event.dispatchTo(this);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
