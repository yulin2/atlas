package org.atlasapi.system;

import java.net.UnknownHostException;
import java.util.List;

import org.atlasapi.persistence.AtlasPersistenceModule;
import org.atlasapi.persistence.bootstrap.ContentBootstrapper;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.LookupResolvingContentResolver;
import org.atlasapi.persistence.content.mongo.MongoContentLister;
import org.atlasapi.persistence.content.mongo.MongoContentResolver;
import org.atlasapi.persistence.content.mongo.MongoTopicStore;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

@Configuration
@Import(AtlasPersistenceModule.class)
public class BootstrapModule {

    private String contentReadMongoHost = Configurer.get("mongo.readOnly.host").get();
    private Integer contentReadMongoPort = Configurer.get("mongo.readOnly.port").toInt();
    private String contentReadMongoName = Configurer.get("mongo.readOnly.dbName").get();
    
    @Autowired AtlasPersistenceModule persistenceModule;
    
    @Bean
    BootstrapController bootstrapController() {
        BootstrapController bootstrapController = new BootstrapController();
        
        bootstrapController.setCassandraContentStore(persistenceModule.contentStore());
        bootstrapController.setCassandraContentBootstrapper(cassandraContentBootstrapper());
        
        bootstrapController.setCassandraTopicStore(persistenceModule.topicStore());
        bootstrapController.setCassandraTopicBootstrapper(cassandraTopicBootstrapper());
        
        return bootstrapController;
    }
    
    @Bean
    IndividualBootstrapController contentBootstrapController() {
        ContentResolver resolver = new LookupResolvingContentResolver(
                new MongoContentResolver(bootstrapMongo()), 
                new MongoLookupEntryStore(bootstrapMongo()));
        return new IndividualBootstrapController(resolver, persistenceModule.contentStore());
    }

    private ContentBootstrapper cassandraTopicBootstrapper() {
        ContentBootstrapper contentBootstrapper = new ContentBootstrapper();
        contentBootstrapper.withTopicListers(new MongoTopicStore(bootstrapMongo()));
        return contentBootstrapper;
    }

    private ContentBootstrapper cassandraContentBootstrapper() {
        ContentBootstrapper contentBootstrapper = new ContentBootstrapper();
        contentBootstrapper.withContentListers(new MongoContentLister(bootstrapMongo()));
        return contentBootstrapper;
    }
    
    @Bean
    public DatabasedMongo bootstrapMongo() {
        Mongo mongo = new Mongo(mongoHosts());
        //mongo.setReadPreference(ReadPreference.secondary());
        return new DatabasedMongo(mongo, contentReadMongoName);
    }
    
    private List<ServerAddress> mongoHosts() {
        Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(splitter.split(contentReadMongoHost), new Function<String, ServerAddress>() {
            @Override
            public ServerAddress apply(String input) {
                try {
                    return new ServerAddress(input, contentReadMongoPort);
                } catch (UnknownHostException e) {
                    return null;
                }
            }
        }), Predicates.notNull()));
    }
    
}
