package org.atlasapi.messaging.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.Session;
import org.atlasapi.persistence.messaging.MessageStore;
import org.atlasapi.messaging.Message;
import org.atlasapi.serialization.json.JsonFactory;
import org.joda.time.DateTime;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class MessageReplayer {

    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();
    private final MessageStore store;
    private final JmsTemplate producer;

    public MessageReplayer(MessageStore store, JmsTemplate producer) {
        this.store = store;
        this.producer = producer;
    }

    public void replay(String destination, DateTime from, DateTime to) {
        for (final Message m : store.get(from, to)) {
            producer.send(destination, new MessageCreator() {

                @Override
                public javax.jms.Message createMessage(Session session) throws JMSException {
                    try {
                        return session.createTextMessage(mapper.writeValueAsString(m));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            });
        }
    }
}