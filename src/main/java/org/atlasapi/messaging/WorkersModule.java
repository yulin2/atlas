package org.atlasapi.messaging;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.ConnectionFactory;
import org.atlasapi.messaging.worker.Worker;
import org.atlasapi.messaging.workers.CassandraReplicator;
import org.atlasapi.messaging.workers.ESIndexer;
import org.atlasapi.messaging.workers.MessageLogger;
import org.atlasapi.messaging.workers.ReplayingWorker;
import org.atlasapi.persistence.content.ContentIndexer;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.messaging.MessageStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 *
 */
@Configuration
public class WorkersModule {

    @Value("${messaging.destination.replicator}")
    private String replicatorDestination;
    @Value("${messaging.consumers.replicator}")
    private int replicatorConsumers;
    @Value("${messaging.destination.indexer}")
    private String indexerDestination;
    @Value("${messaging.consumers.indexer}")
    private int indexerConsumers;
    @Value("${messaging.destination.logger}")
    private String loggerDestination;
    @Value("${messaging.consumers.logger}")
    private int loggerConsumers;
    @Value("${messaging.destination.replay.replicator}")
    private String replicatorReplayDestination;
    @Value("${messaging.destination.replay.indexer}")
    private String indexerReplayDestination;
    @Value("${messaging.replay.interrupt.threshold}")
    private long replayInterruptThreshold;
    @Value("${messaging.enabled}")
    private boolean enabled;
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    @Qualifier(value = "cassandra")
    private ContentWriter cassandraContentWriter;
    @Autowired
    private ContentResolver mongoContentResolver;
    @Autowired
    private MessageStore mongoMessageStore;
    @Autowired
    private ContentIndexer contentIndexer;

    @Bean
    @Lazy(true)
    public ReplayingWorker cassandraReplicator() {
        return new ReplayingWorker(new CassandraReplicator(mongoContentResolver, cassandraContentWriter), replayInterruptThreshold);
    }

    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer cassandraReplicatorMessageListener() {
        return makeContainer(cassandraReplicator(), replicatorDestination, replicatorConsumers, replicatorConsumers);
    }

    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer cassandraReplicatorReplayListener() {
        return makeContainer(cassandraReplicator(), replicatorReplayDestination, 1, 1);
    }

    @Bean
    @Lazy(true)
    public ReplayingWorker esIndexer() {
        return new ReplayingWorker(new ESIndexer(mongoContentResolver, contentIndexer), replayInterruptThreshold);
    }

    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer esIndexerMessageListener() {
        return makeContainer(esIndexer(), indexerDestination, indexerConsumers, indexerConsumers);
    }

    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer esIndexerReplayListener() {
        return makeContainer(esIndexer(), indexerReplayDestination, 1, 1);
    }

    @Bean
    @Lazy(true)
    public Worker messageLogger() {
        return new MessageLogger(mongoMessageStore);
    }

    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer messageLoggerMessageListener() {
        return makeContainer(messageLogger(), loggerDestination, loggerConsumers, loggerConsumers);
    }

    @PostConstruct
    public void start() {
        if (enabled) {
            cassandraReplicator().init();
            esIndexer().init();

            cassandraReplicatorMessageListener().start();
            esIndexerMessageListener().start();
            messageLoggerMessageListener().start();
        }
    }

    @PreDestroy
    public void stop() {
        if (enabled) {
            cassandraReplicator().destroy();
            esIndexer().destroy();
        }
    }

    private DefaultMessageListenerContainer makeContainer(Worker worker, String destination, int consumers, int maxConsumers) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(worker);
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        adapter.setDefaultListenerMethod("onMessage");
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(destination);
        container.setConcurrentConsumers(consumers);
        container.setMaxConcurrentConsumers(maxConsumers);
        container.setMessageListener(adapter);

        return container;
    }
}
