package org.atlasapi.media.content.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.media.entity.ContainerTranslator;
import org.atlasapi.persistence.media.entity.ItemTranslator;
import org.atlasapi.persistence.messaging.event.EntityUpdatedEvent;
import org.atlasapi.serialization.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import javax.jms.TextMessage;

public class EventQueueingContentWriter implements ContentWriter {

    private static final Logger log = LoggerFactory.getLogger(EventQueueingContentWriter.class);
    
    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();
    
	private final JmsTemplate template;
	private final ContentWriter delegate;

    private final ItemTranslator itemTranslator;
    private final ContainerTranslator containerTranslator;
	
	public EventQueueingContentWriter(JmsTemplate template, ContentWriter delegate) {
        NumberToShortStringCodec idCodec = new SubstitutionTableNumberCodec();
		this.template = template;
		this.delegate = delegate;
		this.itemTranslator = new ItemTranslator(idCodec);
		this.containerTranslator = new ContainerTranslator(idCodec);
	}
	
	@Override
	public void createOrUpdate(Item item) {
		delegate.createOrUpdate(item);
	    if (!item.hashChanged(itemTranslator.hashCodeOf(item))) {
            log.debug("{} not changed", item.getCanonicalUri());
            return;
        } 
		enqueueMessageUpdatedEvent(item);
	}

	@Override
	public void createOrUpdate(Container container) {
	    delegate.createOrUpdate(container);
        if (!container.hashChanged(containerTranslator.hashCodeOf(container))) {
            log.debug("{} un-changed", container.getCanonicalUri());
            return;
        } 
	    enqueueMessageUpdatedEvent(container);
	}

    private void enqueueMessageUpdatedEvent(final Content content) {
        template.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(serialize(createEvent(content)));
                return message;
            }
        });
    }

    private EntityUpdatedEvent createEvent(Content content) {
        return new EntityUpdatedEvent(
            null, 
            content.getCanonicalUri(), 
            content.getClass().getSimpleName().toLowerCase(),
            content.getPublisher().key()
        );
    }

    private String serialize(final EntityUpdatedEvent event) {
        String result = null;
        try {
             result = mapper.writeValueAsString(event);
        } catch (Exception e) {
            log.error(event.getEntityId(), e);
        }
        return result;
    }
}
