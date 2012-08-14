package org.atlasapi.messaging.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;
import java.io.IOException;
import java.util.UUID;
import javax.jms.JMSException;
import javax.jms.Session;
import org.atlasapi.messaging.BeginReplayMessage;
import org.atlasapi.messaging.EndReplayMessage;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.persistence.messaging.MessageStore;
import org.atlasapi.messaging.Message;
import org.atlasapi.serialization.json.JsonFactory;
import org.joda.time.DateTime;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class MessageReplayer {

    private final Clock clock = new SystemClock();
    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();
    private final MessageStore store;
    private final JmsTemplate producer;

    public MessageReplayer(MessageStore store, JmsTemplate producer) {
        this.store = store;
        this.producer = producer;
    }

    public void replay(String destination, DateTime from, DateTime to) {
        producer.send(destination, new JsonMessageCreator(new BeginReplayMessage(UUID.randomUUID().toString(), clock.now().getMillis(), null, null, null)));
        for (final Message m : store.get(from, to)) {
            producer.send(destination, new JsonMessageCreator(m));
        }
        producer.send(destination, new JsonMessageCreator(new EndReplayMessage(UUID.randomUUID().toString(), clock.now().getMillis(), null, null, null)));
    }

    private class JsonMessageCreator implements MessageCreator {

        private final Message message;

        public JsonMessageCreator(Message message) {
            this.message = message;
        }

        @Override
        public javax.jms.Message createMessage(Session session) throws JMSException {
            try {
                return session.createTextMessage(mapper.writeValueAsString(message));
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }
}