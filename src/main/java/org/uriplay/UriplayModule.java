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

package org.uriplay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.uriplay.equiv.EquivModule;
import org.uriplay.persistence.MongoContentPersistenceModule;
import org.uriplay.query.QueryModule;
import org.uriplay.remotesite.RemoteSiteModule;

import com.mongodb.Mongo;

@Configuration
@ImportResource({"classpath:uriplay.xml", "beans-scheduling.xml", "beans-tracking.xml"})
@Import({EquivModule.class, QueryModule.class, MongoContentPersistenceModule.class, UriplayFetchModule.class, RemoteSiteModule.class})
public class UriplayModule {
	
	private @Value("${mongo.host}") String mongoHost;

	public @Bean Mongo mongo() {
		try {
			return new Mongo(mongoHost);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
