/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi;

import java.net.UnknownHostException;
import java.util.List;

import org.atlasapi.system.JettyHealthProbe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;
import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.mongodb.Mongo;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

@Configuration
public class AtlasModule {
	
	private final String mongoHost = Configurer.get("mongo.host").get();
	private final String dbName = Configurer.get("mongo.dbName").get();
	private final Parameter processingConfig = Configurer.get("processing.config");

	public @Bean DatabasedMongo databasedMongo() {
	    return new DatabasedMongo(mongo(), dbName);
	}

    public @Bean Mongo mongo() {
        Mongo mongo = new Mongo(mongoHosts());
        if(processingConfig == null || !processingConfig.toBoolean()) {
            mongo.setReadPreference(ReadPreference.secondaryPreferred());
        }
        return mongo;
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
    
    public @Bean HealthProbe jettyHealthProbe() {
        return new JettyHealthProbe();
    }

	public @Bean ContextConfigurer config() {
		ContextConfigurer c = new ContextConfigurer();
		c.init();
		return c;
	}
}
