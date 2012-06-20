package org.atlasapi.messaging;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

import org.atlasapi.messaging.workers.CassandraReplicator;
import org.atlasapi.messaging.workers.MongoRecentChangesStore;
import org.atlasapi.messaging.workers.RecentChangesLog;
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

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

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
    
    @Value("${messaging.destination.recent}")
    private String recentDestination;
    @Value("${messaging.consumers.recent}")
    private int recentConsumers;
    
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
    private DatabasedMongo mongo;

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
    public RecentChangesLog recentChangesLog() {
        return new RecentChangesLog(new MongoRecentChangesStore(mongo));
    }
    
    @Bean
    @Lazy(true)
    public DefaultMessageListenerContainer recentChangesLogListener() {
        MessageListenerAdapter adapter = new MessageListenerAdapter(recentChangesLog());
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        adapter.setDefaultListenerMethod("onMessage");
        container.setConnectionFactory(connectionFactory);
        container.setDestinationName(recentDestination);
        container.setConcurrentConsumers(recentConsumers);
        container.setMaxConcurrentConsumers(recentConsumers);
        container.setMessageListener(adapter);

        return container;
    }
    
    @PostConstruct
    public void start() {
        if (enabled) {
            cassandraReplicator().start();
            recentChangesLogListener().start();
        }
    }
}
