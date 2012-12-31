package org.atlasapi.media.content.util;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.persistence.content.ContentHasher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.serialization.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class MessageQueueingContentWriter implements ContentWriter {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueingContentWriter.class);
    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();
    private final JmsTemplate template;
    private final ContentWriter delegate;
    private final ContentHasher hasher;
    private final Clock clock;

    public MessageQueueingContentWriter(JmsTemplate template, ContentWriter delegate, ContentHasher hasher) {
        this(template, delegate, hasher, new SystemClock());
    }

    public MessageQueueingContentWriter(JmsTemplate template, ContentWriter delegate, ContentHasher hasher, Clock clock) {
        this.template = template;
        this.delegate = delegate;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public void createOrUpdate(Item item) {
        delegate.createOrUpdate(item);
        enqueueMessageUpdatedMessage(item);
    }

    @Override
    public void createOrUpdate(Container container) {
        delegate.createOrUpdate(container);
        enqueueMessageUpdatedMessage(container);
    }

    private void enqueueMessageUpdatedMessage(final Content content) {
        if (!content.hashChanged(hasher.hash(content))) {
            return;
        }
        template.send(new MessageCreator() {

            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(serialize(createEntityUpdatedMessage(content)));
                return message;
            }
        });
    }

    private EntityUpdatedMessage createEntityUpdatedMessage(Content content) {
        return new EntityUpdatedMessage(
                UUID.randomUUID().toString(),
                clock.now().getMillis(),
                content.getCanonicalUri(),
                content.getClass().getSimpleName().toLowerCase(),
                content.getPublisher().key());
    }

    private String serialize(final EntityUpdatedMessage message) {
        String result = null;
        try {
            result = mapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error(message.getEntityId(), e);
        }
        return result;
    }
}
