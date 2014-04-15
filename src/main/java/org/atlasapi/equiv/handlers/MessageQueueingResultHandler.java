package org.atlasapi.equiv.handlers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.messaging.v3.ContentEquivalenceAssertionMessage;
import org.atlasapi.messaging.v3.ContentEquivalenceAssertionMessage.AdjacentRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.queue.BasicMessage;
import com.metabroadcast.common.queue.MessageSender;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;

public class MessageQueueingResultHandler<T extends Content>
        implements EquivalenceResultHandler<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final MessageSender<ContentEquivalenceAssertionMessage> sender;
    private final Timestamper stamper;
    private final Set<Publisher> sources;

    public MessageQueueingResultHandler(MessageSender<ContentEquivalenceAssertionMessage> sender, Iterable<Publisher> sources) {
        this(sender, sources, new SystemClock());
    }
    
    public MessageQueueingResultHandler(MessageSender<ContentEquivalenceAssertionMessage> sender, Iterable<Publisher> sources, 
            Timestamper stamper) {
        this.sender = checkNotNull(sender);
        this.stamper = checkNotNull(stamper);
        this.sources = Sets.newHashSet(sources);
    }
    
    @Override
    public void handle(EquivalenceResult<T> result) {
        try {
            sender.sendMessage(messageFrom(result));;
        } catch (Exception e) {
            log.error("Failed to send equiv update message: " + result.subject(), e);
        }
    }

    private ContentEquivalenceAssertionMessage messageFrom(EquivalenceResult<T> result) {
        String mid = UUID.randomUUID().toString();
        Timestamp ts = stamper.timestamp();
        T subject = result.subject();
        String subjectId = String.valueOf(subject.getId());
        String subjectType = subject.getClass().getSimpleName().toLowerCase();
        String subjectSource = subject.getPublisher().key();
//        return new BasicMessage<Long>("1", Timestamp.of(1L), 1234L);
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
