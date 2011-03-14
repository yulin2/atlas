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

package org.atlasapi.equiv;

import java.util.List;

import javax.annotation.PostConstruct;

import org.atlasapi.equiv.tasks.BrandEquivUpdateTaskRunner;
import org.atlasapi.equiv.www.EquivController;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.AggregateContentListener;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.persistence.equiv.MongoEquivStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.EquivGenerator;
import org.atlasapi.remotesite.freebase.FreebaseBrandEquivGenerator;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class EquivModule {

	private @Autowired DatabasedMongo db;
	private @Autowired AggregateContentListener aggregateContentListener;
	private @Value("${freebase.enabled}") String freebaseEnabled;
	
	@Bean EquivController manualEquivAssignmentController() {
		return new EquivController(store());
	}
	
	public @Bean EquivalentUrlStore store() {
		return new MongoEquivStore(db);
	}
	
	@Bean EquivContentListener equivContentListener() {
	    List<EquivGenerator<Container<?>>> brandEquivGenerators = Boolean.parseBoolean(freebaseEnabled) 
	            ? ImmutableList.<EquivGenerator<Container<?>>>of(new FreebaseBrandEquivGenerator())
	            : ImmutableList.<EquivGenerator<Container<?>>>of();
	    
	    BrandEquivUpdater brandUpdater = new BrandEquivUpdater(brandEquivGenerators, store());
	    ItemEquivUpdater itemUpdater = new ItemEquivUpdater(ImmutableList.<EquivGenerator<Item>>of(), store());
	    
	    EquivContentListener equivContentListener = new EquivContentListener(brandUpdater, itemUpdater);
	    aggregateContentListener.addListener(equivContentListener);
	    return equivContentListener;
	}
	
	private @Autowired SimpleScheduler scheduler;
	private @Autowired ScheduleResolver scheduleResolver;
	private @Autowired AdapterLog log;
	private @Autowired MongoDbBackedContentStore contentStore;
	
	@PostConstruct
	public void scheduleEquivUpdaters() {
	    scheduler.schedule(new BrandEquivUpdateTaskRunner(contentStore, scheduleResolver, log), RepetitionRules.every(Duration.standardHours(10)));
	}
}
