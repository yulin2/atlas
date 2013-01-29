package org.atlasapi.messaging;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.content.ForwardingContentStore;
import org.atlasapi.media.content.util.MessageQueueingContentWriter;
import org.atlasapi.media.util.WriteResult;
import org.atlasapi.serialization.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.fasterxml.jackson.databind.ObjectMapper;


public class MessageQueuingContentStore extends ForwardingContentStore {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueingContentWriter.class);

    private JmsTemplate template;
    private ContentStore delegate;
    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();

    public MessageQueuingContentStore(JmsTemplate template, ContentStore delegate) {
        this.template = template;
        this.delegate = delegate;
    }
    
    @Override
    protected ContentStore delegate() {
        return delegate;
    }

    @Override
    public <C extends Content> WriteResult<C> writeContent(C content) {
        WriteResult<C> result = super.writeContent(content);
        if (result.written()) {
            writeMessage(result);
        }
        return result;
    }

    private <C extends Content> void writeMessage(final WriteResult<C> result) {
        template.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(serialize(createEntityUpdatedMessage(result)));
                return message;
            }
        });
    }
    
    private <C extends Content> EntityUpdatedMessage createEntityUpdatedMessage(WriteResult<C> result) {
        return new EntityUpdatedMessage(
                UUID.randomUUID().toString(),
                result.getWriteTime().getMillis(),
                result.getResource().getId().toString(),
                result.getClass().getSimpleName().toLowerCase(),
                result.getResource().getPublisher().key());
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
