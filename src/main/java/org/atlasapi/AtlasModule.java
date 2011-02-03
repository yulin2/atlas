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

import org.atlasapi.application.ApplicationModule;
import org.atlasapi.equiv.EquivModule;
import org.atlasapi.feeds.AtlasFeedsModule;
import org.atlasapi.logging.AtlasLoggingModule;
import org.atlasapi.logging.HealthModule;
import org.atlasapi.persistence.MongoContentPersistenceModule;
import org.atlasapi.query.QueryModule;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.mongodb.Mongo;

@Configuration
@Import({AtlasLoggingModule.class, AtlasWebModule.class, EquivModule.class, QueryModule.class, MongoContentPersistenceModule.class, AtlasFetchModule.class, RemoteSiteModule.class, AtlasFeedsModule.class, HealthModule.class, ApplicationModule.class})
public class AtlasModule {
	
	private final String mongoHost = Configurer.get("mongo.host").get();
	private final String dbName = Configurer.get("mongo.dbName").get();

	public @Bean DatabasedMongo mongo() {
		try {
			return new DatabasedMongo(new Mongo(mongoHost), dbName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	 
    public @Bean ContextConfigurer config() {
		ContextConfigurer c = new ContextConfigurer();
		c.init();
		return c;
	}
}
