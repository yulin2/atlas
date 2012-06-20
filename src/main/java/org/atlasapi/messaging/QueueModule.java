package org.atlasapi.messaging;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class QueueModule {
    
    @Value("${messaging.broker.url}")
    private String brokerUrl;

    public @Bean JmsTemplate virutalTopicChangesTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(activemqConnectionFactory());
        jmsTemplate.setDefaultDestinationName("VirtualTopic.Changes");
        return jmsTemplate;
    }
    
    @Bean
    @Lazy(true)
    public ConnectionFactory activemqConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        return cachingConnectionFactory;
    }
}
