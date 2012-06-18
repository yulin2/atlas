package org.atlasapi.messaging;

import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.atlasapi.messaging.workers.CassandraReplicator;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 *
 */
@Configuration
public class MessagingModule {

    @Value("${messaging.broker.url}")
    private String brokerUrl;
    @Value("${messaging.destination.replicator}")
    private String replicatorDestination;
    @Value("${messaging.consumers.replicator}")
    private int replicatorConsumers;
    //
    @Autowired
    @Qualifier(value = "cassandra")
    private ContentWriter cassandraContentWriter;
    @Autowired
    private ContentResolver mongoContentResolver;

    @Bean
    public ConnectionFactory activemqConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        return cachingConnectionFactory;
    }

    @Bean
    public DefaultMessageListenerContainer cassandraReplicator() {
        CassandraReplicator cassandraReplicator = new CassandraReplicator(mongoContentResolver, cassandraContentWriter);
        MessageListenerAdapter adapter = new MessageListenerAdapter(cassandraReplicator);
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();

        adapter.setDefaultListenerMethod("onMessage");
        container.setConnectionFactory(activemqConnectionFactory());
        container.setDestinationName(replicatorDestination);
        container.setConcurrentConsumers(replicatorConsumers);
        container.setMaxConcurrentConsumers(replicatorConsumers);
        container.setMessageListener(adapter);

        container.start();

        return container;
    }
}
