package org.atlasapi.messaging.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import org.atlasapi.messaging.BeginReplayMessage;
import org.atlasapi.messaging.EndReplayMessage;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.atlasapi.messaging.Message;
import org.atlasapi.messaging.ReplayMessage;
import org.atlasapi.messaging.worker.Worker;
import org.atlasapi.serialization.json.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Abstract {@link org.atlasapi.persistence.messaging.worker.Worker} class
 * providing
 * coalescing capabilities based on the message type and entity id, useful for
 * message deduping.
 */
public abstract class AbstractCoalescingWorker implements Worker {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //
    private final ObjectMapper mapper = JsonFactory.makeJsonMapper();
    //
    private final ReentrantLock coalesceLock = new ReentrantLock();
    private final AtomicInteger coalesceSize = new AtomicInteger();
    private final ExecutorService coalesceExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(this.getClass().getName() + " - Executor").setDaemon(true).build());
    private final ScheduledExecutorService coalesceScheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat(this.getClass().getName() + " - Scheduler").setDaemon(true).build());
    private final JmsTransactionManager coalesceTx;
    private final JmsCustomTemplate coalesceQueue;
    private final int coalesceMillisThreshold;
    private final int coalesceSizeThreshold;

    /**
     * Constructor to enable message coalescing capabilities.
     *
     * @param connectionFactory
     * @param coalesceQueue
     * @param coalesceMillisThreshold
     * @param coalesceSizeThreshold
     */
    public AbstractCoalescingWorker(ConnectionFactory connectionFactory, String coalesceQueue, int coalesceMillisThreshold, int coalesceSizeThreshold) {
        this.coalesceMillisThreshold = coalesceMillisThreshold;
        this.coalesceSizeThreshold = coalesceSizeThreshold;
        this.coalesceTx = new JmsTransactionManager(connectionFactory);
        this.coalesceQueue = new JmsCustomTemplate(connectionFactory);
        this.coalesceQueue.setDefaultDestinationName(coalesceQueue);
        this.coalesceQueue.setReceiveTimeout(100);
    }

    /**
     * Constructor to disable message coalescing capabilities.
     */
    public AbstractCoalescingWorker() {
        this.coalesceMillisThreshold = 0;
        this.coalesceSizeThreshold = 0;
        this.coalesceTx = null;
        this.coalesceQueue = null;
    }

    public void start() {
        if (coalesceSizeThreshold > 0) {
            coalesceScheduler.scheduleAtFixedRate(new CoalesceRunner(), coalesceMillisThreshold, coalesceMillisThreshold, TimeUnit.MILLISECONDS);
        }
    }

    public void onMessage(String message) {
        try {
            Message event = mapper.readValue(message, Message.class);
            if (event.canCoalesce() && coalesceSizeThreshold > 0) {
                log.debug("Coalescing message: {}", message);
                coalesceQueue.convertAndSend(message);
                // Coalesces only at a given threshold:
                if (coalesceSize.incrementAndGet() >= coalesceSizeThreshold) {
                    // Lock and double check to protect against concurrent consumers reaching together the threshold:
                    synchronized (this) {
                        if (coalesceSize.get() >= coalesceSizeThreshold) {
                            // Decrement the threshold in a locked block so that only the first consumer reaching it actually coalesces:
                            coalesceSize.addAndGet(-coalesceSizeThreshold);
                            coalesceExecutor.submit(new CoalesceRunner());
                        }
                    }
                }
            } else {
                log.debug("Dispatching message: {}", message);
                event.dispatchTo(this);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public void process(EntityUpdatedMessage message) {
    }

    @Override
    public void process(BeginReplayMessage message) {
    }

    @Override
    public void process(EndReplayMessage message) {
    }

    @Override
    public void process(ReplayMessage message) {
    }

    private void doCoalesce() {
        if (coalesceLock.tryLock()) {
            TransactionStatus tx = coalesceTx.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
            try {
                Map<String, Message> events = new LinkedHashMap<String, Message>();
                String received = (String) coalesceQueue.receiveAndConvert();
                while (received != null) {
                    Message current = mapper.readValue(received, Message.class);
                    String key = makeCoalescingKey(current);
                    if (events.containsKey(key)) {
                        events.remove(key);
                    }
                    events.put(key, current);
                    if (events.size() < coalesceSizeThreshold) {
                        received = (String) coalesceQueue.receiveAndConvert();
                    } else {
                        received = null;
                    }
                }
                for (Map.Entry<String, Message> current : events.entrySet()) {
                    log.debug("Dispatching coalesced message: {}", current.getValue());
                    current.getValue().dispatchTo(this);
                }
                coalesceQueue.closeThreadLocalConsumer();
                coalesceTx.commit(tx);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                try {
                    coalesceQueue.closeThreadLocalConsumer();
                } catch (JMSException ex2) {
                    log.warn(ex2.getMessage(), ex2);
                }
                coalesceTx.rollback(tx);
            } finally {
                coalesceLock.unlock();
            }
        }
    }

    private String makeCoalescingKey(Message message) {
        return message.getClass().getName() + ":" + message.getEntityId();
    }

    private static class UnclosedMessageConsumer implements MessageConsumer {

        private final MessageConsumer delegate;
        private volatile boolean closed;

        public UnclosedMessageConsumer(MessageConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getMessageSelector() throws JMSException {
            return delegate.getMessageSelector();
        }

        @Override
        public MessageListener getMessageListener() throws JMSException {
            return delegate.getMessageListener();
        }

        @Override
        public void setMessageListener(MessageListener ml) throws JMSException {
            delegate.setMessageListener(ml);
        }

        @Override
        public javax.jms.Message receive() throws JMSException {
            return delegate.receive();
        }

        @Override
        public javax.jms.Message receive(long l) throws JMSException {
            return delegate.receive(l);
        }

        @Override
        public javax.jms.Message receiveNoWait() throws JMSException {
            return delegate.receiveNoWait();
        }

        @Override
        public void close() throws JMSException {
        }

        public MessageConsumer getDelegate() {
            return delegate;
        }
    }

    private static class JmsCustomTemplate extends JmsTemplate {

        private static final ThreadLocal<UnclosedMessageConsumer> LOCAL_CONSUMER = new ThreadLocal<UnclosedMessageConsumer>();

        public JmsCustomTemplate(ConnectionFactory connectionFactory) {
            super(connectionFactory);
        }

        public void closeThreadLocalConsumer() throws JMSException {
            if (LOCAL_CONSUMER.get() != null) {
                LOCAL_CONSUMER.get().getDelegate().close();
                LOCAL_CONSUMER.set(null);
            }
        }

        @Override
        protected MessageConsumer createConsumer(Session session, Destination destination, String messageSelector) throws JMSException {
            if (LOCAL_CONSUMER.get() == null) {
                LOCAL_CONSUMER.set(new UnclosedMessageConsumer(super.createConsumer(session, destination, messageSelector)));
            }
            return LOCAL_CONSUMER.get();
        }
    }

    private class CoalesceRunner implements Runnable {

        @Override
        public void run() {
            doCoalesce();
        }
    }
}
