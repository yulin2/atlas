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

import static org.atlasapi.equiv.results.EquivalenceResultBuilder.resultBuilder;
import static org.atlasapi.equiv.update.ResultWritingEquivalenceUpdater.resultWriter;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.generators.ItemBasedContainerEquivalenceGenerator;
import org.atlasapi.equiv.generators.TitleMatchingContainerEquivalenceGenerator;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.combining.AddingEquivalenceCombiner;
import org.atlasapi.equiv.results.combining.ScalingEquivalenceCombiner;
import org.atlasapi.equiv.results.extractors.MinimumScoreEquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor;
import org.atlasapi.equiv.results.persistence.MongoEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
import org.atlasapi.equiv.results.probe.EquivalenceProbeStore;
import org.atlasapi.equiv.results.probe.EquivalenceResultProbeController;
import org.atlasapi.equiv.results.probe.MongoEquivalenceProbeStore;
import org.atlasapi.equiv.results.www.EquivalenceResultController;
import org.atlasapi.equiv.results.www.RecentResultController;
import org.atlasapi.equiv.update.BasicEquivalenceUpdater;
import org.atlasapi.equiv.update.ContentEquivalenceUpdateController;
import org.atlasapi.equiv.update.ContentEquivalenceUpdateTask;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.equiv.update.RootEquivalenceUpdater;
import org.atlasapi.equiv.www.EquivController;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.AggregateContentListener;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.RetrospectiveContentLister;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.equiv.EquivalentUrlStore;
import org.atlasapi.persistence.equiv.MongoEquivStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.EquivGenerator;
import org.atlasapi.remotesite.freebase.FreebaseBrandEquivGenerator;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.time.DayOfWeek;

@Configuration
public class EquivModule {

	private @Autowired DatabasedMongo db;
	private @Autowired AggregateContentListener aggregateContentListener;
	private @Value("${freebase.enabled}") String freebaseEnabled;
	private @Value("${seesaw.equiv.enabled}") String seesawEquivEnabled;
	private @Value("${equiv.updater.enabled}") String updaterEnabled;
	private @Value("${itunes.equiv.enabled}") String itunesEquivEnabled;
	
	@Bean EquivController manualEquivAssignmentController() {
		return new EquivController(store());
	}
	
	public @Bean EquivalentUrlStore store() {
		return new MongoEquivStore(db);
	}
	
	@Bean EquivContentListener equivContentListener() {
	    ImmutableList.Builder<EquivGenerator<Container<?>>> brandEquivGenerators = ImmutableList.builder(); 
	    ImmutableList.Builder<EquivGenerator<Item>> itemEquivGenerators = ImmutableList.builder();
	    if (Boolean.parseBoolean(freebaseEnabled)) {
	        brandEquivGenerators.add(new FreebaseBrandEquivGenerator());
	    }
	    if (Boolean.parseBoolean(seesawEquivEnabled)) {
	        PublisherCachingBrandEquivGenerator seesawBrandEquivGenerator = new PublisherCachingBrandEquivGenerator(Publisher.SEESAW, new MongoDbBackedContentStore(db));
            brandEquivGenerators.add(seesawBrandEquivGenerator);
	        itemEquivGenerators.add(new ItemDelegatingToBrandEquivGenerator(seesawBrandEquivGenerator));
	    }
	    if (Boolean.parseBoolean(itunesEquivEnabled)) {
	        PublisherCachingBrandEquivGenerator itunesBrandEquivGenerator = new PublisherCachingBrandEquivGenerator(Publisher.ITUNES, new MongoDbBackedContentStore(db));
            brandEquivGenerators.add(itunesBrandEquivGenerator);
            itemEquivGenerators.add(new ItemDelegatingToBrandEquivGenerator(itunesBrandEquivGenerator));
	    }
	    
	    BrandEquivUpdater brandUpdater = new BrandEquivUpdater(brandEquivGenerators.build(), store());
	    ItemEquivUpdater itemUpdater = new ItemEquivUpdater(itemEquivGenerators.build(), store());
	    
	    EquivContentListener equivContentListener = new EquivContentListener(brandUpdater, itemUpdater);
	    aggregateContentListener.addListener(equivContentListener);
	    return equivContentListener;
	}
	
//	private @Autowired SimpleScheduler scheduler;
//	
//	@PostConstruct
//	public void scheduleEquivUpdaters() {
//	    if(Boolean.valueOf(updaterEnabled)) {
//	        scheduler.schedule(new PaEquivUpdateTask(new MongoDbBackedContentStore(db), itemBasedBrandEquivUpdater(), filmEquivUpdater(), equivResultStore(), log), RepetitionRules.every(Duration.standardHours(10)));
//	    }
//	}
//	
//	@Bean ItemEquivGenerator itemEquivGenerator() {
//	    ItemEquivGenerator broadcastEquivGen = new BroadcastMatchingItemEquivGenerator(scheduleResolver);
//	    return new DelegatingItemEquivGenerator(ImmutableList.of(broadcastEquivGen));
//	}
//	
//	@Bean ItemBasedBrandEquivUpdater itemBasedBrandEquivUpdater() {
//	    MongoDbBackedContentStore mongoDbBackedContentStore = new MongoDbBackedContentStore(db);
//	    return new ItemBasedBrandEquivUpdater(itemEquivGenerator(), mongoDbBackedContentStore, mongoDbBackedContentStore).writesResults(true);
//	}
//	
//	@Bean FilmEquivUpdater filmEquivUpdater() {
//	    return new FilmEquivUpdater(searchResolver, new MongoDbBackedContentStore(db));
//	}
//	
//	@Bean EquivResultStore equivResultStore() {
//	    return new CachingEquivResultStore(new MongoEquivResultStore(db));
//	}
//	
//	@Bean EquivResultController equivResultController() {
//	    return new EquivResultController(equivResultStore());
//	}
//	
//	@Bean SingleBrandEquivUpdateController singleBrandUpdater() {
//	    return new SingleBrandEquivUpdateController(itemBasedBrandEquivUpdater(), new MongoDbBackedContentStore(db));
//	}
	private static final RepetitionRule EQUIVALENCE_REPETITION = RepetitionRules.weekly(DayOfWeek.MONDAY, new LocalTime(9, 00));
    
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired RetrospectiveContentLister contentLister;
    private @Autowired ContentResolver contentResolver;
    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler taskScheduler;

    public @Bean RecentEquivalenceResultStore equivalenceResultStore() {
        return new RecentEquivalenceResultStore(new MongoEquivalenceResultStore(db));
    }
    
    public @Bean ContentEquivalenceUpdater<Item> itemUpdater() {
        Set<ContentEquivalenceGenerator<Item>> itemGenerators = ImmutableSet.<ContentEquivalenceGenerator<Item>>of(
                new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(1))
        );
        EquivalenceResultBuilder<Item> resultBuilder = standardResultBuilder(itemGenerators.size());
        return resultWriter(new BasicEquivalenceUpdater<Item>(itemGenerators, resultBuilder, log), equivalenceResultStore());
    }

    private <T extends Content> EquivalenceResultBuilder<T> standardResultBuilder(int calculators) {
        return resultBuilder(
                new ScalingEquivalenceCombiner<T>(AddingEquivalenceCombiner.<T>create(), 1.0/calculators), 
                MinimumScoreEquivalenceExtractor.minimumFrom(PercentThresholdEquivalenceExtractor.<T>fromPercent(90), 0.0));
    }
    
    public @Bean ContentEquivalenceUpdater<Container<?>> containerUpdater() {
        Set<ContentEquivalenceGenerator<Container<?>>> containerGenerators = ImmutableSet.<ContentEquivalenceGenerator<Container<?>>>of(
                new ItemBasedContainerEquivalenceGenerator(itemUpdater()),
                new TitleMatchingContainerEquivalenceGenerator(searchResolver)
        );
        EquivalenceResultBuilder<Container<?>> resultBuilder = standardResultBuilder(containerGenerators.size());
        return resultWriter(new BasicEquivalenceUpdater<Container<?>>(containerGenerators, resultBuilder, log), equivalenceResultStore());
    }

    public @Bean RootEquivalenceUpdater contentUpdater() {
        return new RootEquivalenceUpdater(containerUpdater(), itemUpdater());
    }
    
    public @Bean ContentEquivalenceUpdateTask updateTask() {
        return new ContentEquivalenceUpdateTask(contentLister, contentUpdater(), log, db);
    }
    
    @PostConstruct
    public void scheduleUpdater() {
        if(Boolean.parseBoolean(updaterEnabled)) {
            taskScheduler.schedule(updateTask().withName("Content Equivalence Updater"), EQUIVALENCE_REPETITION);
        }
    }
    
    //Controllers...
    public @Bean ContentEquivalenceUpdateController updateController() {
        return new ContentEquivalenceUpdateController(contentUpdater(), contentResolver);
    }
    
    public @Bean EquivalenceResultController resultController() {
        return new EquivalenceResultController(equivalenceResultStore());
    }
    
    public @Bean RecentResultController recentController() {
        return new RecentResultController(equivalenceResultStore());
    }
    
    //Probes...
    public @Bean EquivalenceProbeStore equivProbeStore() { 
        return new MongoEquivalenceProbeStore(db);
    }
    
    public @Bean EquivalenceResultProbeController equivProbeController() {
        return new EquivalenceResultProbeController(equivalenceResultStore(), equivProbeStore());
    }
}
