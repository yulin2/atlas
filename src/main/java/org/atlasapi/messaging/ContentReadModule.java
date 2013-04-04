package org.atlasapi.messaging;

import java.net.UnknownHostException;

import javax.jms.ConnectionFactory;

import org.atlasapi.messaging.worker.Worker;
import org.atlasapi.messaging.workers.ContentReadWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.persistence.content.KnownTypeContentResolver;
import org.atlasapi.persistence.content.LookupResolvingContentResolver;
import org.atlasapi.persistence.content.NullContentResolver;
import org.atlasapi.persistence.content.mongo.MongoContentResolver;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.mongodb.Mongo;

@Configuration
public class ContentReadModule {
    
    private String contentReadDestination = Configurer.get("messaging.destination.content").get();
    private String contentReadMongoHost = Configurer.get("mongo.readOnly.host").get();
    private Integer contentReadMongoPort = Configurer.get("mongo.readOnly.port").toInt();
    private String contentReadMongoName = Configurer.get("mongo.readOnly.dbName").get();

    @Autowired private ConnectionFactory connectionFactory;
    @Autowired private ContentWriter contentWriter;
    
    @Bean
    @Lazy(true)
    DefaultMessageListenerContainer contentReadWriter() {
        return makeContainer(contentReadWriterWorker(), contentReadDestination, 1, 1);
    }
    
    private Worker contentReadWriterWorker() {
        return new ContentReadWriter(contentResolver(), contentWriter);
    }
    
    private ContentResolver contentResolver() {
        if (Strings.isNullOrEmpty(contentReadMongoHost)
            || Strings.isNullOrEmpty(contentReadMongoName)) {
            return NullContentResolver.get();
        }
        Mongo mongo = mongo();
        DatabasedMongo mongoDb = new DatabasedMongo(mongo, contentReadMongoName);
        KnownTypeContentResolver contentResolver = new MongoContentResolver(mongoDb);
        LookupEntryStore lookupEntryStore = new MongoLookupEntryStore(mongoDb);
        return new LookupResolvingContentResolver(contentResolver, lookupEntryStore);
    }

    private Mongo mongo() {
        try {
            return new Mongo(contentReadMongoHost, contentReadMongoPort);
        } catch (UnknownHostException e) {
            throw Throwables.propagate(e);
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
