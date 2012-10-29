/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atlasapi.messaging.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.serialization.json.JsonFactory;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 */
public class AbstractCoalescingWorkerTest {

    private static final String BROKER = "vm://localhost?broker.persistent=false";
    private static final String COALESCE_QUEUE = "coalesce";
    private static final String MAIN_QUEUE = "main";
    //
    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();
    private ActiveMQConnectionFactory connectionFactory;
    private JmsTemplate mainQueue;
    private DefaultMessageListenerContainer listener;

    @Before
    public void setUp() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(BROKER);
        //
        mainQueue = new JmsTemplate(connectionFactory);
        mainQueue.setDefaultDestinationName(MAIN_QUEUE);
    }

    @After
    public void tearDown() {
        listener.shutdown();
    }

    @Test
    public void testCoalesceBySize() throws InterruptedException, IOException {
        final CountDownLatch processLatch = new CountDownLatch(2);
        final List<EntityUpdatedMessage> processedMessages = new CopyOnWriteArrayList<EntityUpdatedMessage>();
        //
        listener = makeListener(new AbstractCoalescingWorker(connectionFactory, COALESCE_QUEUE, 60000, 3) {

            @Override
            public void process(EntityUpdatedMessage message) {
                processedMessages.add(message);
                processLatch.countDown();
            }
        });
        listener.initialize();
        listener.start();
        //
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test1", 1l, "Test1", "", "")));
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test2", 2l, "Test2", "", "")));
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test2", 3l, "Test2", "", "")));
        //
        assertTrue(processLatch.await(60, TimeUnit.SECONDS));
        assertEquals(Long.valueOf(1), processedMessages.get(0).getTimestamp());
        assertEquals(Long.valueOf(3), processedMessages.get(1).getTimestamp());
    }

    @Test
    public void testCoalesceByTime() throws InterruptedException, IOException {
        final CountDownLatch processLatch = new CountDownLatch(2);
        final List<EntityUpdatedMessage> processedMessages = new CopyOnWriteArrayList<EntityUpdatedMessage>();
        //
        listener = makeListener(new AbstractCoalescingWorker(connectionFactory, COALESCE_QUEUE, 5000, 10) {

            @Override
            public void process(EntityUpdatedMessage message) {
                processedMessages.add(message);
                processLatch.countDown();
            }
        });
        listener.initialize();
        listener.start();
        //
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test1", 1l, "Test1", "", "")));
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test2", 2l, "Test2", "", "")));
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test2", 3l, "Test2", "", "")));
        //
        assertFalse(processLatch.await(3, TimeUnit.SECONDS));
        //
        assertTrue(processLatch.await(60, TimeUnit.SECONDS));
        assertEquals(Long.valueOf(1), processedMessages.get(0).getTimestamp());
        assertEquals(Long.valueOf(3), processedMessages.get(1).getTimestamp());
    }

    @Test
    public void testRedeliveryOnError() throws InterruptedException, IOException {
        final CountDownLatch processLatch = new CountDownLatch(2);
        final CountDownLatch failLatch = new CountDownLatch(1);
        final AtomicBoolean failOnFirstTime = new AtomicBoolean(true);
        //
        listener = makeListener(new AbstractCoalescingWorker(connectionFactory, COALESCE_QUEUE, 3000, 1) {

            @Override
            public void process(EntityUpdatedMessage message) {
                if (failOnFirstTime.get()) {
                    failLatch.countDown();
                    failOnFirstTime.set(false);
                    throw new RuntimeException();
                } else {
                    processLatch.countDown();
                }
            }
        });
        listener.initialize();
        listener.start();
        //
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test1", 1l, "Test1", "", "")));
        //
        assertTrue(failLatch.await(600, TimeUnit.SECONDS));
        //
        mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("Test2", 2l, "Test2", "", "")));
        //
        assertTrue(processLatch.await(600, TimeUnit.SECONDS));
    }

    @Test
    public void testCanCoalesceAllMessages() throws InterruptedException, IOException {
        final int total = 1000;
        final CountDownLatch processLatch = new CountDownLatch(total);
        //
        listener = makeListener(new AbstractCoalescingWorker(connectionFactory, COALESCE_QUEUE, 60000, 1000) {

            @Override
            public void process(EntityUpdatedMessage message) {
                processLatch.countDown();
            }
        });
        listener.initialize();
        listener.start();
        //
        for (int i = 0; i <= total; i++) {
            mainQueue.convertAndSend(mapper.writeValueAsString(new EntityUpdatedMessage("" + i, 1l, "" + i, "", "")));
        }
        //
        assertTrue(processLatch.await(60, TimeUnit.SECONDS));
    }

    private DefaultMessageListenerContainer makeListener(AbstractCoalescingWorker worker) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(worker);
        DefaultMessageListenerContainer listener = new DefaultMessageListenerContainer();
        adapter.setDefaultListenerMethod("onMessage");
        listener.setConnectionFactory(connectionFactory);
        listener.setDestinationName(MAIN_QUEUE);
        listener.setMessageListener(adapter);
        worker.start();
        return listener;
    }
}
