package org.atlasapi.messaging;

import com.metabroadcast.common.properties.Configurer;
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

    private String indexerDestination = Configurer.get("messaging.destination.indexer").get();
    private int indexerConsumers = Integer.parseInt(Configurer.get("messaging.consumers.indexer").get());
    private String indexerReplayDestination = Configurer.get("messaging.destination.replay.indexer").get();
    private String indexerCoalesceQueue = Configurer.get("messaging.indexer.coalesce.queue").get();
    private int indexerCoalesceSize = Integer.parseInt(Configurer.get("messaging.indexer.coalesce.size").get());
    private int indexerCoalesceTime = Integer.parseInt(Configurer.get("messaging.indexer.coalesce.time").get());
    private String loggerDestination = Configurer.get("messaging.destination.logger").get();
    private int loggerConsumers = Integer.parseInt(Configurer.get("messaging.consumers.logger").get());
    private long replayInterruptThreshold = Long.parseLong(Configurer.get("messaging.replay.interrupt.threshold").get());
    private boolean enabled = Boolean.parseBoolean(Configurer.get("messaging.enabled").get());
    //
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private ContentIndexer contentIndexer;
    @Autowired
    private ContentResolver mongoContentResolver;
    @Autowired
    private MessageStore mongoMessageStore;

    @Bean
    @Lazy(true)
    public ReplayingWorker esIndexer() {
        return new ReplayingWorker(new ESIndexer(mongoContentResolver, contentIndexer), connectionFactory, indexerCoalesceQueue, indexerCoalesceTime, indexerCoalesceSize);
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
            esIndexer().start();
            
            esIndexerMessageListener().initialize();
            esIndexerMessageListener().start();
            
            messageLoggerMessageListener().initialize();
            messageLoggerMessageListener().start();
        }
    }

    @PreDestroy
    public void stop() {
        if (enabled) {
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
