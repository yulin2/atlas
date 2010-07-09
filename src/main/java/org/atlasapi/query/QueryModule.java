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

package org.atlasapi.query;

import org.atlasapi.persistence.content.AggregateContentListener;
import org.atlasapi.persistence.content.ContentListener;
import org.atlasapi.persistence.content.QueueingContentListener;
import org.atlasapi.persistence.content.mongo.MongoDBQueryExecutor;
import org.atlasapi.persistence.content.mongo.MongoRoughSearch;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.query.content.UniqueContentForUriQueryExecutor;
import org.atlasapi.query.content.UriFetchingQueryExecutor;
import org.atlasapi.query.content.fuzzy.DefuzzingQueryExecutor;
import org.atlasapi.query.content.fuzzy.InMemoryFuzzySearcher;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryModule {

	private @Autowired @Qualifier("contentResolver") CanonicalisingFetcher localOrRemoteFetcher;
	private @Autowired MongoRoughSearch contentStore;
	private @Autowired AggregateContentListener aggregateContentListener;
	
	@Bean KnownTypeQueryExecutor mongoQueryExecutor() {
		return new UniqueContentForUriQueryExecutor(new MongoDBQueryExecutor(contentStore));
	}

	@Bean KnownTypeQueryExecutor mongoDbQueryExcutorThatFiltersUriQueries() {
		MongoDBQueryExecutor executor = new MongoDBQueryExecutor(contentStore);
		executor.setFilterUriQueries(true);
		return executor;
	}

	@Bean KnownTypeQueryExecutor queryExecutor() {
		return new UriFetchingQueryExecutor(localOrRemoteFetcher, new DefuzzingQueryExecutor(mongoQueryExecutor(), mongoDbQueryExcutorThatFiltersUriQueries(), titleSearcher()));
	}
	
	@Bean InMemoryFuzzySearcher titleSearcher() {
		return new InMemoryFuzzySearcher();
	}
    
    @Bean(destroyMethod="shutdown") ContentListener queueingContentListener() {
        QueueingContentListener queueingContentListener = new QueueingContentListener(titleSearcher());
        aggregateContentListener.addListener(queueingContentListener);
        return queueingContentListener;
    }
}
