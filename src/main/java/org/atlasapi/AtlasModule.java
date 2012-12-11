package org.atlasapi;

import java.net.UnknownHostException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;
import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

// README This class has the ony purpose to isolate and have the configurer load first, otherwise some subtle conflicts happen.
@Configuration
public class AtlasModule {
	private final String mongoHost = Configurer.get("mongo.host").get();
	private final String dbName = Configurer.get("mongo.dbName").get();
	private final Parameter processingConfig = Configurer.get("processing.config");
	
	public @Bean DatabasedMongo databasedMongo() {
	    return new DatabasedMongo(mongo(), dbName);
	}
    @Bean
    public ContextConfigurer config() {
        ContextConfigurer c = new ContextConfigurer();
        c.init();
        return c;
    }
    
    public @Bean Mongo mongo() {
        Mongo mongo = new Mongo(mongoHosts());
        if(processingConfig == null || !processingConfig.toBoolean()) {
            mongo.slaveOk();
        }
        return mongo;
    }
    
    public @Bean Mongo adminMongo() {
        Mongo adminMongo = new Mongo(mongoHosts());
        return adminMongo;
    }

    private List<ServerAddress> mongoHosts() {
        Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(splitter.split(mongoHost), new Function<String, ServerAddress>() {

            @Override
            public ServerAddress apply(String input) {
                try {
                    return new ServerAddress(input, 27017);
                } catch (UnknownHostException e) {
                    return null;
                }
            }
        }), Predicates.notNull()));
    }
}
