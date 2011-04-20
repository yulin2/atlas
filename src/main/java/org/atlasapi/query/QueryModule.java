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
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.mongo.MongoDBQueryExecutor;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.query.content.ApplicationConfigurationQueryExecutor;
import org.atlasapi.query.content.CurieResolvingQueryExecutor;
import org.atlasapi.query.content.UriFetchingQueryExecutor;
import org.atlasapi.query.content.fuzzy.FuzzySearcher;
import org.atlasapi.query.content.fuzzy.RemoteFuzzySearcher;
import org.atlasapi.query.content.people.ContentResolvingPeopleResolver;
import org.atlasapi.query.content.schedule.BroadcastRemovingScheduleOverlapListener;
import org.atlasapi.query.content.schedule.NastyRenameChannelJob;
import org.atlasapi.query.content.schedule.ScheduleOverlapListener;
import org.atlasapi.query.content.schedule.ScheduleOverlapResolver;
import org.atlasapi.query.content.schedule.ThreadedScheduleOverlapListener;
import org.atlasapi.query.content.search.ContentResolvingSearcher;
import org.atlasapi.query.content.search.DummySearcher;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.atlasapi.query.v2.PeopleController;
import org.atlasapi.query.v2.QueryController;
import org.atlasapi.query.v2.ScheduleController;
import org.atlasapi.query.v2.SearchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Strings;

@Configuration
public class QueryModule {

	private @Autowired @Qualifier("contentResolver") CanonicalisingFetcher localOrRemoteFetcher;
	private @Autowired MongoDbBackedContentStore store;
	private @Autowired ScheduleResolver scheduleResolver;
	private @Autowired PeopleResolver peopleResolver;
	private @Value("${applications.enabled}") String applicationsEnabled;
	
	private @Autowired ApplicationConfigurationFetcher configFetcher;
	private @Autowired AdapterLog log;
	
	private @Value("${atlas.search.host}") String searchHost;

	@Bean KnownTypeQueryExecutor queryExecutor() {
		KnownTypeQueryExecutor defuzzingExecutor = new MongoDBQueryExecutor(store);
		
		UriFetchingQueryExecutor uriFetching = new UriFetchingQueryExecutor(localOrRemoteFetcher, defuzzingExecutor);
		
	    CurieResolvingQueryExecutor curieResolving = new CurieResolvingQueryExecutor(uriFetching);
		
		MergeOnOutputQueryExecutor brandMerger = new MergeOnOutputQueryExecutor(curieResolving);
	    if (Boolean.parseBoolean(applicationsEnabled)) {
	        return new ApplicationConfigurationQueryExecutor(brandMerger);
	    } else {
	        return brandMerger;
	    }
	}
	
	@Bean SearchResolver searchResolver() {
	    if (! Strings.isNullOrEmpty(searchHost)) {
    	    FuzzySearcher titleSearcher = new RemoteFuzzySearcher(searchHost);
    	    return new ContentResolvingSearcher(titleSearcher, queryExecutor());
	    }
	    
	    return new DummySearcher();
	}
	
	@Bean QueryController queryController() {
		return new QueryController(queryExecutor(), configFetcher, log, atlasModelOutputter());
	}
	
	@Bean ScheduleOverlapListener scheduleOverlapListener() {
	    BroadcastRemovingScheduleOverlapListener broadcastRemovingListener = new BroadcastRemovingScheduleOverlapListener(store, store);
	    return new ThreadedScheduleOverlapListener(broadcastRemovingListener, log);
	}
	
	@Bean ScheduleController schedulerController() {
	    ScheduleOverlapResolver resolver = new ScheduleOverlapResolver(scheduleResolver, scheduleOverlapListener(), log);
	    return new ScheduleController(resolver, configFetcher, log, atlasModelOutputter());
	}
	
	@Bean PeopleController peopleController() {
	    return new PeopleController(new ContentResolvingPeopleResolver(peopleResolver, queryExecutor()), configFetcher, log, atlasModelOutputter());
	}
	
	@Bean SearchController searchController() {
	    return new SearchController(searchResolver(), configFetcher, log, atlasModelOutputter());
	}

	@Bean AtlasModelWriter atlasModelOutputter() {
		return new DispatchingAtlasModelWriter();
	}
	
	@Bean NastyRenameChannelJob nastyRenameChannelJob() {
	    return new NastyRenameChannelJob(store);
	}
}
