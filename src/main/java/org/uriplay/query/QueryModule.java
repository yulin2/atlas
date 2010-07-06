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
import org.springframework.context.annotation.Import;
import org.uriplay.persistence.UriplayPersistenceModule;
import org.uriplay.persistence.content.mongo.MongoDBQueryExecutor;
import org.uriplay.persistence.content.mongo.MongoRoughSearch;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
import org.uriplay.query.content.UriFetchingQueryExecutor;
import org.uriplay.query.content.fuzzy.DefuzzingQueryExecutor;
import org.uriplay.query.content.fuzzy.InMemoryFuzzySearcher;
import org.uriplay.query.uri.canonical.CanonicalisingLocalRemoteFetcher;

@Configuration
@Import(UriplayPersistenceModule.class)
public class QueryModule {

	private @Autowired @Qualifier("contentResolver") CanonicalisingLocalRemoteFetcher localOrRemoteFetcher;
	private @Autowired MongoRoughSearch contentStore;
	
	@Bean KnownTypeQueryExecutor mongoQueryExecutor() {
		return new MongoDBQueryExecutor(contentStore);
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
}
