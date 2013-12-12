package org.atlasapi.equiv.handlers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.messaging.v3.ContentEquivalenceAssertionMessage;
import org.atlasapi.messaging.v3.ContentEquivalenceAssertionMessage.AdjacentRef;
import org.atlasapi.serialization.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamper;

public class MessageQueueingResultHandler<T extends Content>
        implements EquivalenceResultHandler<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final JmsTemplate template;
    private final Timestamper stamper;
    private final Set<Publisher> sources;

    private final ObjectMapper serializer = JsonFactory.makeJsonMapper();

    public MessageQueueingResultHandler(JmsTemplate template, Iterable<Publisher> sources) {
        this(template, sources, new SystemClock());
    }
    
    public MessageQueueingResultHandler(JmsTemplate template, Iterable<Publisher> sources, 
            Timestamper stamper) {
        this.template = checkNotNull(template);
        this.stamper = checkNotNull(stamper);
        this.sources = Sets.newHashSet(sources);
    }
    
    @Override
    public void handle(EquivalenceResult<T> result) {
        try {
            final byte[] msgBytes = serialize(messageFrom(result));
            template.send(new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    BytesMessage msg = session.createBytesMessage();
                    msg.writeBytes(msgBytes);
                    return msg;
                }
            });
        } catch (Exception e) {
            log.error("Failed to send equiv update message: " + result.subject(), e);
        }
    }

    private byte[] serialize(ContentEquivalenceAssertionMessage msg) throws Exception {
        return serializer.writeValueAsBytes(msg);
    }

    private ContentEquivalenceAssertionMessage messageFrom(EquivalenceResult<T> result) {
        String mid = UUID.randomUUID().toString();
        long ts = stamper.timestamp().millis();
        T subject = result.subject();
        String subjectId = String.valueOf(subject.getId());
        String subjectType = subject.getClass().getSimpleName().toLowerCase();
        String subjectSource = subject.getPublisher().key();
        return new ContentEquivalenceAssertionMessage(mid, ts, 
                subjectId, subjectType, subjectSource, 
                adjacents(result), sources);
    }

    private List<AdjacentRef> adjacents(EquivalenceResult<T> result) {
        return Lists.newArrayList(Collections2.transform(result.strongEquivalences().values(),
            new Function<ScoredCandidate<T>, AdjacentRef>() {
                @Override
                public AdjacentRef apply(ScoredCandidate<T> input) {
                    T cand = input.candidate();
                    return new AdjacentRef(cand.getId(),
                        cand.getClass().getSimpleName().toLowerCase(),
                        cand.getPublisher());
                }
            }
        ));
    }
    
}
