package org.atlasapi.messaging;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

import org.atlasapi.messaging.workers.CassandraReplicator;
import org.atlasapi.messaging.workers.RecentChangesLogger;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import org.atlasapi.messaging.workers.RecentChangeStore;

/**
 *
 */
@Configuration
@Import(MessagingModule.class)
public class WorkersModule {

    @Value("${messaging.destination.replicator}")
    private String replicatorDestination;
    @Value("${messaging.consumers.replicator}")
    private int replicatorConsumers;
    
    @Value("${messaging.destination.logger}")
    private String loggerDestination;
    @Value("${messaging.consumers.logger}")
    private int loggerConsumers;
    
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
    private RecentChangeStore recentChangesStore;

    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer cassandraReplicator() {
        CassandraReplicator cassandraReplicator = new CassandraReplicator(mongoContentResolver, cassandraContentWriter);
        MessageListenerAdapter adapter = new MessageListenerAdapter(cassandraReplicator);
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        adapter.setDefaultListenerMethod("onMessage");
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(replicatorDestination);
        container.setConcurrentConsumers(replicatorConsumers);
        container.setMaxConcurrentConsumers(replicatorConsumers);
        container.setMessageListener(adapter);

        return container;
    }
    
    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer recentChangesLogger() {
        RecentChangesLogger recentChangesLogger = new RecentChangesLogger(recentChangesStore);
        MessageListenerAdapter adapter = new MessageListenerAdapter(recentChangesLogger);
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        adapter.setDefaultListenerMethod("onMessage");
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(loggerDestination);
        container.setConcurrentConsumers(loggerConsumers);
        container.setMaxConcurrentConsumers(loggerConsumers);
        container.setMessageListener(adapter);

        return container;
    }
    
    @PostConstruct
    public void start() {
        if (enabled) {
            cassandraReplicator().start();
            recentChangesLogger().start();
        }
    }
}
