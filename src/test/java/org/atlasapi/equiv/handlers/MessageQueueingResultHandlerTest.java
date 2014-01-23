package org.atlasapi.equiv.handlers;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.messaging.v3.ContentEquivalenceAssertionMessage;
import org.atlasapi.messaging.v3.QueueFactory;
import org.atlasapi.messaging.worker.v3.AbstractWorker;
import org.atlasapi.messaging.worker.v3.Worker;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class MessageQueueingResultHandlerTest {

    private MessageQueueingResultHandler<Item> handler;
    private QueueFactory qf;

    @Before
    public void setup() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost");
        qf = new QueueFactory(cf, cf, "Atlas.Owl.Test");
        JmsTemplate template = qf.makeVirtualTopicProducer("Equiv.Assert");
        handler = new MessageQueueingResultHandler<Item>(template, Publisher.all());
    }

    @Test
    public void testHandleSendsAMessage() throws Exception {
        
        Item subject = new Item("s","s",Publisher.BBC);
        subject.setId(1L);
        Item equivalent = new Item("e","e",Publisher.PA);
        equivalent.setId(2L);
        List<ScoredCandidates<Item>> scores = ImmutableList.of();
        ScoredCandidates<Item> combined = DefaultScoredCandidates.<Item>fromSource("src").build();
        Map<Publisher, ScoredCandidate<Item>> strong = ImmutableMap.of(
            Publisher.PA, ScoredCandidate.valueOf(equivalent, Score.ONE));
        ReadableDescription desc = new DefaultDescription();
        
        final CountDownLatch latch = new CountDownLatch(1);
        
        Worker worker = new AbstractWorker() {
            
            @Override
            public void process(ContentEquivalenceAssertionMessage equivalenceAssertionMessage) {
                latch.countDown();
            }
          
        };
        DefaultMessageListenerContainer consumer = qf.makeVirtualTopicConsumer(worker, "Deserializer", "Equiv.Assert", 1, 1);
        consumer.initialize();
        consumer.start();

        handler.handle(new EquivalenceResult<Item>(subject, scores, combined, strong, desc));
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

}
