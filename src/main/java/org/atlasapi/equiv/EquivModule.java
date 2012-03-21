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

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static org.atlasapi.equiv.update.ContainerEquivalenceUpdater.containerUpdater;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.BBC_REDUX;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.media.entity.Publisher.ITUNES;
import static org.atlasapi.media.entity.Publisher.ITV;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.atlasapi.media.entity.Publisher.PREVIEW_NETWORKS;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContainerChildEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.generators.FilmEquivalenceGenerator;
import org.atlasapi.equiv.generators.TitleMatchingEquivalenceScoringGenerator;
import org.atlasapi.equiv.handlers.BroadcastingEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.handlers.LookupWritingEquivalenceHandler;
import org.atlasapi.equiv.handlers.ResultWritingEquivalenceHandler;
import org.atlasapi.equiv.results.ConfiguredEquivalenceResultBuilder;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.persistence.InMemoryLiveEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.MongoEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
import org.atlasapi.equiv.results.probe.EquivalenceProbeStore;
import org.atlasapi.equiv.results.probe.EquivalenceResultProbeController;
import org.atlasapi.equiv.results.probe.MongoEquivalenceProbeStore;
import org.atlasapi.equiv.results.www.EquivalenceResultController;
import org.atlasapi.equiv.results.www.RecentResultController;
import org.atlasapi.equiv.scorers.ContainerChildEquivalenceScorer;
import org.atlasapi.equiv.scorers.ContainerHierarchyMatchingEquivalenceScorer;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.equiv.scorers.SequenceItemEquivalenceScorer;
import org.atlasapi.equiv.scorers.TitleMatchingItemEquivalenceScorer;
import org.atlasapi.equiv.update.ContainerEquivalenceUpdater;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.equiv.update.ItemEquivalenceUpdater;
import org.atlasapi.equiv.update.PublisherSwitchingContentEquivalenceUpdater;
import org.atlasapi.equiv.update.ResultHandlingEquivalenceUpdater;
import org.atlasapi.equiv.update.RootEquivalenceUpdater;
import org.atlasapi.equiv.update.tasks.ContentEquivalenceUpdateTask;
import org.atlasapi.equiv.update.tasks.FilmEquivalenceUpdateTask;
import org.atlasapi.equiv.update.tasks.FilteringContentLister;
import org.atlasapi.equiv.update.tasks.MongoScheduleTaskProgressStore;
import org.atlasapi.equiv.update.www.ContentEquivalenceUpdateController;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.schedule.mongo.MongoScheduleStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.lookup.LookupWriter;
import org.atlasapi.persistence.lookup.TransitiveLookupWriter;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.atlasapi.query.content.schedule.ManualScheduleUpdateController;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class EquivModule {

	private @Value("${equiv.updater.enabled}") String updaterEnabled;

	private static final RepetitionRule EQUIVALENCE_REPETITION = RepetitionRules.daily(new LocalTime(9, 00));
    
    private @Autowired MongoScheduleStore scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired ContentLister contentLister;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired DatabasedMongo db;

    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler taskScheduler;

    public @Bean RecentEquivalenceResultStore equivalenceResultStore() {
        return new RecentEquivalenceResultStore(new MongoEquivalenceResultStore(db));
    }
    
    public @Bean LiveEquivalenceResultStore liveResultsStore() {
        return new InMemoryLiveEquivalenceResultStore();
    }
    
    public @Bean LookupWriter lookupWriter() {
        return new TransitiveLookupWriter(new MongoLookupEntryStore(db));
    }

    protected <T extends Content> LookupWritingEquivalenceHandler<T> lookupWritingEquivalenceHandler(Iterable<Publisher> publishers) {
        return new LookupWritingEquivalenceHandler<T>(lookupWriter(), publishers);
    }

    public @Bean MongoScheduleTaskProgressStore progressStore() {
        return new MongoScheduleTaskProgressStore(db);
    }
    
    private <T extends Content> EquivalenceResultHandler<T> standardResultHandlers(Iterable<Publisher> publishers) {
        return new BroadcastingEquivalenceResultHandler<T>(ImmutableList.of(
                this.<T>lookupWritingEquivalenceHandler(publishers),
                new ResultWritingEquivalenceHandler<T>(equivalenceResultStore())
        ));
    }
    
    private <T extends Content> ResultHandlingEquivalenceUpdater<T> resultHandlingUpdater(ContentEquivalenceUpdater<T> delegate, Iterable<Publisher> publishers) {
        return new ResultHandlingEquivalenceUpdater<T>(delegate, this.<T>standardResultHandlers(publishers));
    }
    
    public @Bean ItemEquivalenceUpdater<Item> standardItemUpdater() {
        Set<ContentEquivalenceGenerator<Item>> itemGenerators = ImmutableSet.<ContentEquivalenceGenerator<Item>>of(
                new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, channelResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(10))
        );
        
        Set<ContentEquivalenceScorer<Item>> itemScorers = ImmutableSet.<ContentEquivalenceScorer<Item>>of(
                new TitleMatchingItemEquivalenceScorer(),
                new SequenceItemEquivalenceScorer()
        );
        
        EquivalenceResultBuilder<Item> resultBuilder = new ConfiguredEquivalenceResultBuilder<Item>();
        
        return new ItemEquivalenceUpdater<Item>(itemGenerators, itemScorers, resultBuilder, log);
    }
    
    public ContainerEquivalenceUpdater.Builder containerUpdaterBuilder(Iterable<Publisher> publishers) {
        return containerUpdater(contentResolver, liveResultsStore(), new ConfiguredEquivalenceResultBuilder<Container>(), this.<Item>standardResultHandlers(publishers), log);
    }

    public @Bean ResultHandlingEquivalenceUpdater<Content> contentUpdater() {
        //Generally acceptable publishers.
        Set<Publisher> acceptablePublishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()),ImmutableSet.of(PREVIEW_NETWORKS, BBC_REDUX));
        
        TitleMatchingEquivalenceScoringGenerator titleScoringGenerator = new TitleMatchingEquivalenceScoringGenerator(searchResolver);
        
        ItemEquivalenceUpdater<Item> itemUpdater = standardItemUpdater();
        
        ImmutableMap.Builder<Publisher, ContentEquivalenceUpdater<Content>> publisherUpdaters = ImmutableMap.builder();
        
        RootEquivalenceUpdater standardContainerEquivalenceUpdater = new RootEquivalenceUpdater(containerUpdaterBuilder(acceptablePublishers)
                .withGenerators(ImmutableSet.of(
                        titleScoringGenerator,
                        new ContainerChildEquivalenceGenerator(contentResolver, itemUpdater, liveResultsStore(), log)))
                .withScorer(titleScoringGenerator)
                .build(), itemUpdater);
        
        ImmutableSet<Publisher> nonStandardPublishers = ImmutableSet.of(ITUNES, BBC_REDUX);
        for (Publisher publisher : Iterables.filter(ImmutableList.copyOf(Publisher.values()), not(in(nonStandardPublishers)))) {
                publisherUpdaters.put(publisher, standardContainerEquivalenceUpdater);
        }
        
        publisherUpdaters.put(BBC_REDUX, new RootEquivalenceUpdater(containerUpdaterBuilder(Sets.union(acceptablePublishers, ImmutableSet.of(BBC_REDUX)))
                .withGenerators(ImmutableSet.of(
                        titleScoringGenerator,
                        new ContainerChildEquivalenceGenerator(contentResolver, itemUpdater, liveResultsStore(), log)))
                .withScorer(titleScoringGenerator)
                .build(), itemUpdater)
        );
        
        publisherUpdaters.put(ITUNES, new RootEquivalenceUpdater(
                containerUpdaterBuilder(acceptablePublishers)
                    .withGenerator(titleScoringGenerator)
                    .withScorers(ImmutableSet.of(titleScoringGenerator, 
                            new ContainerChildEquivalenceScorer(itemUpdater, liveResultsStore(), contentResolver, log),
                            new ContainerHierarchyMatchingEquivalenceScorer(contentResolver)))
                    .build(), 
                itemUpdater));
        
        return resultHandlingUpdater(new PublisherSwitchingContentEquivalenceUpdater(publisherUpdaters.build()), acceptablePublishers);
    }

    public TitleMatchingEquivalenceScoringGenerator defaultScaledTitleScoringGenerator() {
        return new TitleMatchingEquivalenceScoringGenerator(searchResolver);
    }
    
    private ContentEquivalenceUpdateTask publisherUpdateTask(final Publisher... publishers) {
        ContentLister filteringLister = new FilteringContentLister(contentLister, new Predicate<Content>() {
            @Override
            public boolean apply(Content input) {
                //Ignore PA Films.
                return !(Publisher.PA.equals(input.getPublisher()) && input instanceof Film);
            }
        });
        return new ContentEquivalenceUpdateTask(filteringLister, contentUpdater(), log, progressStore()).forPublishers(publishers);
    }
    
    public @Bean FilmEquivalenceUpdateTask filmUpdateTask() {
        return new FilmEquivalenceUpdateTask(contentLister, filmUpdater(), log, progressStore());
    }
    
    public @Bean ContentEquivalenceUpdater<Film> filmUpdater() {
        Set<ContentEquivalenceGenerator<Film>> generators = ImmutableSet.<ContentEquivalenceGenerator<Film>>of(new FilmEquivalenceGenerator(searchResolver));
        Set<ContentEquivalenceScorer<Film>> scorers = ImmutableSet.<ContentEquivalenceScorer<Film>>of();
        EquivalenceResultBuilder<Film> resultBuilder = new ConfiguredEquivalenceResultBuilder<Film>();
        
        ContentEquivalenceUpdater<Film> updater = new ItemEquivalenceUpdater<Film>(generators, scorers, resultBuilder, log);
        return resultHandlingUpdater(updater, ImmutableSet.of(Publisher.PREVIEW_NETWORKS));
    }
    
    @PostConstruct
    public void scheduleUpdater() {
        if(Boolean.parseBoolean(updaterEnabled)) {
            taskScheduler.schedule(publisherUpdateTask(BBC, C4, ITV, BBC_REDUX).withName("BBC/C4/ITV/Redux Equivalence Updater"), EQUIVALENCE_REPETITION);
            taskScheduler.schedule(publisherUpdateTask(PA).withName("PA Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(BBC).withName("BBC Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(C4).withName("C4 Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(ITV).withName("ITV Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(BBC_REDUX).withName("Redux Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(ITUNES).withName("Itunes Equivalence Updater"), RepetitionRules.NEVER);
            
            taskScheduler.schedule(filmUpdateTask().withName("Film Equivalence Updater"), EQUIVALENCE_REPETITION);
            
            taskScheduler.schedule(childRefUpdateTask().forPublishers(Publisher.BBC).withName("BBC Child Ref Update"), RepetitionRules.NEVER);
            taskScheduler.schedule(childRefUpdateTask().forPublishers(Publisher.PA).withName("PA Child Ref Update"), RepetitionRules.NEVER);
            taskScheduler.schedule(childRefUpdateTask().forPublishers(publishersApartFrom(Publisher.BBC, Publisher.PA)).withName("Other Publishers Child Ref Update"), RepetitionRules.NEVER);
        }
    }

    private Publisher[] publishersApartFrom(Publisher...publishers) {
        SetView<Publisher> remainingPublishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.copyOf(publishers));
        return remainingPublishers.toArray(new Publisher[remainingPublishers.size()]);
    }

    protected @Bean ChildRefUpdateController childRefUpdateController() {
        return new ChildRefUpdateController(childRefUpdateTask(), contentResolver);
    }
    
    protected ChildRefUpdateTask childRefUpdateTask() {
        return new ChildRefUpdateTask(contentLister, contentResolver, db, progressStore(), log);
    }
    
    //Controllers...
    public @Bean ContentEquivalenceUpdateController contentEquivalenceUpdateController() {
        return new ContentEquivalenceUpdateController(contentUpdater(), filmUpdater(), contentResolver, log);
    }
    
    public @Bean EquivalenceResultController resultEquivalenceResultController() {
        return new EquivalenceResultController(equivalenceResultStore(), equivProbeStore(), contentResolver);
    }
    
    public @Bean RecentResultController recentEquivalenceResultController() {
        return new RecentResultController(equivalenceResultStore());
    }
    
    //Probes...
    public @Bean EquivalenceProbeStore equivProbeStore() { 
        return new MongoEquivalenceProbeStore(db);
    }
    
    public @Bean EquivalenceResultProbeController equivProbeController() {
        return new EquivalenceResultProbeController(equivalenceResultStore(), equivProbeStore());
    }

    @Bean ManualScheduleUpdateController scheduleUpdateController() {
        return new ManualScheduleUpdateController(scheduleResolver, contentResolver, channelResolver);
    }
    
}
