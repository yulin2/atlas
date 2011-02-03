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

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.equiv.query.MergeOnOutputQueryExecutor;
import org.atlasapi.feeds.www.DispatchingAtlasModelWriter;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.mongo.MongoDBQueryExecutor;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.AToZUriSource;
import org.atlasapi.query.content.ApplicationConfigurationQueryExecutor;
import org.atlasapi.query.content.CurieResolvingQueryExecutor;
import org.atlasapi.query.content.UriFetchingQueryExecutor;
import org.atlasapi.query.content.fuzzy.DefuzzingQueryExecutor;
import org.atlasapi.query.content.fuzzy.FuzzySearcher;
import org.atlasapi.query.content.fuzzy.RemoteFuzzySearcher;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.atlasapi.query.v2.QueryController;
import org.atlasapi.remotesite.health.BroadcasterProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.health.HealthProbe;

@Configuration
public class QueryModule {

	private @Autowired @Qualifier("contentResolver") CanonicalisingFetcher localOrRemoteFetcher;
	private @Autowired MongoDbBackedContentStore store;
	private @Value("${applications.enabled}") String applicationsEnabled;
	
	private @Autowired ApplicationConfigurationFetcher configFetcher;
	private @Autowired AdapterLog log;
	
	private @Value("${atlas.search.host}") String searchHost;
	
	@Bean KnownTypeQueryExecutor mongoQueryExecutor() {
		return new MongoDBQueryExecutor(store);
	}
	
	public @Bean HealthProbe c4Probe() {
		return new BroadcasterProbe(Publisher.C4, new AToZUriSource("http://www.channel4.com/programmes/atoz/", "", true), store);
	}
	
	public @Bean HealthProbe bbcProbe() {
		return new BroadcasterProbe(Publisher.BBC, ImmutableList.of(
				"http://www.bbc.co.uk/programmes/b006m86d", //Eastenders
				"http://www.bbc.co.uk/programmes/b006mf4b", //Spooks
				"http://www.bbc.co.uk/programmes/b006t1q9", //Question Time
				"http://www.bbc.co.uk/programmes/b006qj9z", //Today
				"http://www.bbc.co.uk/programmes/b006md2v", //Pierre Bleu
				"http://www.bbc.co.uk/programmes/b0071b63", //L'apprentice
				"http://www.bbc.co.uk/programmes/b007t9yb", //Match of the Day 2
				"http://www.bbc.co.uk/programmes/b0087g39", //Helicopter Heroes
				"http://www.bbc.co.uk/programmes/b006mk1s", //Mastermind
				"http://www.bbc.co.uk/programmes/b006wknd" //Rob da Bank, yeh...
		), store);
	}

	@Bean KnownTypeQueryExecutor mongoDbQueryExcutorThatFiltersUriQueries() {
		MongoDBQueryExecutor executor = new MongoDBQueryExecutor(store);
		executor.setFilterUriQueries(true);
		return executor;
	}

	@Bean KnownTypeQueryExecutor queryExecutor() {
		KnownTypeQueryExecutor defuzzingExecutor = mongoQueryExecutor();
		
		if (!Strings.isNullOrEmpty(searchHost)) {
			FuzzySearcher titleSearcher = new RemoteFuzzySearcher(searchHost);
		    defuzzingExecutor = new DefuzzingQueryExecutor(mongoQueryExecutor(), mongoDbQueryExcutorThatFiltersUriQueries(), titleSearcher);
		}
		
		UriFetchingQueryExecutor uriFetching = new UriFetchingQueryExecutor(localOrRemoteFetcher, defuzzingExecutor);
		
	    CurieResolvingQueryExecutor curieResolving = new CurieResolvingQueryExecutor(uriFetching);
		
		MergeOnOutputQueryExecutor brandMerger = new MergeOnOutputQueryExecutor(curieResolving);
	    if (Boolean.parseBoolean(applicationsEnabled)) {
	        return new ApplicationConfigurationQueryExecutor(brandMerger);
	    } else {
	        return brandMerger;
	    }
	}
	
	@Bean QueryController queryController() {
		return new QueryController(queryExecutor(), configFetcher, log, atlasModelOutputter());
	}

	@Bean AtlasModelWriter atlasModelOutputter() {
		return new DispatchingAtlasModelWriter();
	}
	
}
