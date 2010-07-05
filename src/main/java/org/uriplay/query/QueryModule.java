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

package org.uriplay.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uriplay.media.entity.Content;
import org.uriplay.persistence.content.AggregateContentListener;
import org.uriplay.persistence.content.ContentListener;
import org.uriplay.persistence.content.EventFiringContentStore;
import org.uriplay.persistence.content.MongoDbBackedContentBootstrapper;
import org.uriplay.persistence.content.MutableContentStore;
import org.uriplay.persistence.content.QueueingContentListener;
import org.uriplay.persistence.content.mongo.MongoDBQueryExecutor;
import org.uriplay.persistence.content.mongo.MongoDbBackedContentStore;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.tracking.ContentMentionStore;
import org.uriplay.persistence.tracking.MongoDBBackedContentMentionStore;
import org.uriplay.query.content.UriFetchingQueryExecutor;
import org.uriplay.query.content.fuzzy.DefuzzingQueryExecutor;
import org.uriplay.query.content.fuzzy.InMemoryFuzzySearcher;

import com.google.common.collect.ImmutableList;
import com.mongodb.Mongo;

@Configuration
public class QueryModule {

	private @Autowired Mongo mongo;

	private @Autowired @Qualifier("localOrRemoteFetcher") Fetcher<Content> localOrRemoteFetcher;
	
	public @Bean ContentMentionStore contentMentionStore() {
		return new MongoDBBackedContentMentionStore(mongo, "uriplay");
	}
	
	public @Bean MutableContentStore contentStore() {
		return new EventFiringContentStore(mongoContentStore(), contentListener());
	}	
	
	@Bean KnownTypeQueryExecutor mongoQueryExecutor() {
		return new MongoDBQueryExecutor(mongoContentStore());
	}

	@Bean KnownTypeQueryExecutor mongoDbQueryExcutorThatFiltersUriQueries() {
		MongoDBQueryExecutor executor = new MongoDBQueryExecutor(mongoContentStore());
		executor.setFilterUriQueries(true);
		return executor;
	}

	@Bean MongoDbBackedContentStore mongoContentStore() {
		return new MongoDbBackedContentStore(mongo, "uriplay");
	}
	
	@Bean KnownTypeQueryExecutor queryExecutor() {
		return new UriFetchingQueryExecutor(localOrRemoteFetcher, new DefuzzingQueryExecutor(mongoQueryExecutor(), mongoDbQueryExcutorThatFiltersUriQueries(), titleSearcher()));
	}
	
	@Bean InMemoryFuzzySearcher titleSearcher() {
		return new InMemoryFuzzySearcher();
	}
	
	@Bean(destroyMethod="shutdown") ContentListener contentListener() {
		return new QueueingContentListener(new AggregateContentListener(ImmutableList.of(titleSearcher())));
	}
	
	@Bean MongoDbBackedContentBootstrapper contentBootstrapper() {
		return new MongoDbBackedContentBootstrapper(contentListener(), mongoContentStore());
	}

}
