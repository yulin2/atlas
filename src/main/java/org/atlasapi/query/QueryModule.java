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

import org.atlasapi.equiv.query.MergeOnOutputQueryExecutor;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.mongo.MongoContentResolver;
import org.atlasapi.persistence.content.mongo.MongoContentTables;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.lookup.BasicLookupResolver;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.atlasapi.query.content.ApplicationConfigurationQueryExecutor;
import org.atlasapi.query.content.CurieResolvingQueryExecutor;
import org.atlasapi.query.content.LookupResolvingQueryExecutor;
import org.atlasapi.query.content.UriFetchingQueryExecutor;
import org.atlasapi.query.content.fuzzy.FuzzySearcher;
import org.atlasapi.query.content.fuzzy.RemoteFuzzySearcher;
import org.atlasapi.query.content.search.ContentResolvingSearcher;
import org.atlasapi.query.content.search.DummySearcher;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Strings;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class QueryModule {

	private @Autowired @Qualifier("remoteSiteContentResolver") CanonicalisingFetcher localOrRemoteFetcher;
	
	private @Autowired DatabasedMongo db;
	
	private @Value("${applications.enabled}") String applicationsEnabled;
	private @Value("${atlas.search.host}") String searchHost;

	@Bean KnownTypeQueryExecutor queryExecutor() {
		KnownTypeQueryExecutor queryExecutor = new LookupResolvingQueryExecutor(new MongoContentResolver(new MongoContentTables(db)), new BasicLookupResolver(new MongoLookupEntryStore(db)));
		
		queryExecutor = new UriFetchingQueryExecutor(localOrRemoteFetcher, queryExecutor);
		
	    queryExecutor = new CurieResolvingQueryExecutor(queryExecutor);
		
	    queryExecutor = new MergeOnOutputQueryExecutor(queryExecutor);
	    
	    return Boolean.parseBoolean(applicationsEnabled) ? new ApplicationConfigurationQueryExecutor(queryExecutor) : queryExecutor;
	}
	
	@Bean SearchResolver searchResolver() {
	    if (! Strings.isNullOrEmpty(searchHost)) {
    	    FuzzySearcher titleSearcher = new RemoteFuzzySearcher(searchHost);
    	    return new ContentResolvingSearcher(titleSearcher, queryExecutor());
	    }
	    
	    return new DummySearcher();
	}
}
