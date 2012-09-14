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
import static org.atlasapi.equiv.generators.AliasResolvingEquivalenceGenerator.aliasResolvingGenerator;
import static org.atlasapi.equiv.update.ContainerEquivalenceUpdater.containerUpdater;
import static org.atlasapi.media.entity.Publisher.BBC_REDUX;
import static org.atlasapi.media.entity.Publisher.FACEBOOK;
import static org.atlasapi.media.entity.Publisher.ITUNES;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.atlasapi.media.entity.Publisher.PREVIEW_NETWORKS;
import static org.atlasapi.media.entity.Publisher.RADIO_TIMES;
import static org.atlasapi.persistence.lookup.TransitiveLookupWriter.generatedTransitiveLookupWriter;

import java.io.File;
import java.util.Set;

import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContainerChildEquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerator;
import org.atlasapi.equiv.generators.FilmEquivalenceGenerator;
import org.atlasapi.equiv.generators.RadioTimesFilmEquivalenceGenerator;
import org.atlasapi.equiv.generators.TitleMatchingEquivalenceScoringGenerator;
import org.atlasapi.equiv.handlers.BroadcastingEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.handlers.LookupWritingEquivalenceHandler;
import org.atlasapi.equiv.handlers.ResultWritingEquivalenceHandler;
import org.atlasapi.equiv.results.ConfiguredEquivalenceResultBuilder;
import org.atlasapi.equiv.results.DefaultEquivalenceResultBuilder;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.combining.NullScoreAwareAveragingCombiner;
import org.atlasapi.equiv.results.extractors.MinimumScoreEquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.SpecializationMatchingEquivalenceExtractor;
import org.atlasapi.equiv.results.persistence.FileEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.InMemoryLiveEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
import org.atlasapi.equiv.scorers.ContainerChildEquivalenceScorer;
import org.atlasapi.equiv.scorers.ContainerHierarchyMatchingEquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorer;
import org.atlasapi.equiv.scorers.SequenceItemEquivalenceScorer;
import org.atlasapi.equiv.scorers.TitleMatchingItemEquivalenceScorer;
import org.atlasapi.equiv.update.ContainerEquivalenceUpdater;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.equiv.update.ItemEquivalenceUpdater;
import org.atlasapi.equiv.update.NullEquivalenceUpdater;
import org.atlasapi.equiv.update.PublisherSwitchingContentEquivalenceUpdater;
import org.atlasapi.equiv.update.ResultHandlingEquivalenceUpdater;
import org.atlasapi.equiv.update.RootEquivalenceUpdater;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.schedule.mongo.MongoScheduleStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class EquivModule {

	private @Value("${equiv.results.directory}") String equivResultsDirectory;
    
    private @Autowired MongoScheduleStore scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired DatabasedMongo db;
    private @Autowired AdapterLog log;

    public @Bean RecentEquivalenceResultStore equivalenceResultStore() {
        return new RecentEquivalenceResultStore(new FileEquivalenceResultStore(new File(equivResultsDirectory)));
    }
    
    public @Bean LiveEquivalenceResultStore liveResultsStore() {
        return new InMemoryLiveEquivalenceResultStore();
    }

    protected <T extends Content> LookupWritingEquivalenceHandler<T> lookupWritingEquivalenceHandler(Iterable<Publisher> publishers) {
        return new LookupWritingEquivalenceHandler<T>(generatedTransitiveLookupWriter(new MongoLookupEntryStore(db)), publishers);
    }

    private <T extends Content> EquivalenceResultHandler<T> standardResultHandlers(Iterable<Publisher> publishers) {
        return new BroadcastingEquivalenceResultHandler<T>(ImmutableList.of(
                this.<T>lookupWritingEquivalenceHandler(publishers),
                new ResultWritingEquivalenceHandler<T>(equivalenceResultStore())
        ));
    }
    
    private <T extends Content> ResultHandlingEquivalenceUpdater<T> resultHandlingUpdater(EquivalenceUpdater<T> delegate, Iterable<Publisher> publishers) {
        return new ResultHandlingEquivalenceUpdater<T>(delegate, this.<T>standardResultHandlers(publishers));
    }
    
    public ItemEquivalenceUpdater<Item> standardItemUpdater() {
        Set<EquivalenceGenerator<Item>> itemGenerators = ImmutableSet.<EquivalenceGenerator<Item>>of(
                new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, channelResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(10))
        );
        
        Set<EquivalenceScorer<Item>> itemScorers = ImmutableSet.of(
                new TitleMatchingItemEquivalenceScorer(),
                new SequenceItemEquivalenceScorer()
        );
        
        EquivalenceResultBuilder<Item> resultBuilder = new ConfiguredEquivalenceResultBuilder<Item>();
        
        return new ItemEquivalenceUpdater<Item>(itemGenerators, itemScorers, resultBuilder, log);
    }
    
    public ContainerEquivalenceUpdater.Builder containerUpdaterBuilder(Iterable<Publisher> publishers) {
        return containerUpdater(contentResolver, liveResultsStore(), new ConfiguredEquivalenceResultBuilder<Container>(), this.<Item>standardResultHandlers(publishers), log);
    }

    public @Bean EquivalenceUpdater<Content> contentUpdater() {
        //Generally acceptable publishers.
        Set<Publisher> acceptablePublishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.of(PREVIEW_NETWORKS, BBC_REDUX, RADIO_TIMES));
        
        TitleMatchingEquivalenceScoringGenerator titleScoringGenerator = new TitleMatchingEquivalenceScoringGenerator(searchResolver);
        
        ItemEquivalenceUpdater<Item> itemUpdater = standardItemUpdater();
        
        ImmutableMap.Builder<Publisher, EquivalenceUpdater<Content>> publisherUpdaters = ImmutableMap.builder();
        
        EquivalenceUpdater<Content> standardContainerEquivalenceUpdater = resultHandlingUpdater(new RootEquivalenceUpdater(containerUpdaterBuilder(acceptablePublishers)
                .withGenerators(ImmutableSet.of(
                        titleScoringGenerator,
                        new ContainerChildEquivalenceGenerator(contentResolver, itemUpdater, liveResultsStore(), log)))
                .withScorer(titleScoringGenerator)
                .build(), itemUpdater), acceptablePublishers);
        
        ImmutableSet<Publisher> nonStandardPublishers = ImmutableSet.of(ITUNES, BBC_REDUX, RADIO_TIMES, FACEBOOK);
        for (Publisher publisher : Iterables.filter(ImmutableList.copyOf(Publisher.values()), not(in(nonStandardPublishers)))) {
                publisherUpdaters.put(publisher, standardContainerEquivalenceUpdater);
        }
        
        publisherUpdaters.put(RADIO_TIMES, resultHandlingUpdater(new RootEquivalenceUpdater(NullEquivalenceUpdater.<Container>get(),
                ItemEquivalenceUpdater.builder(new ConfiguredEquivalenceResultBuilder<Item>(), log)
                    .withGenerators(ImmutableSet.of(new RadioTimesFilmEquivalenceGenerator(contentResolver), new FilmEquivalenceGenerator(searchResolver)))
                    .build()),ImmutableSet.of(RADIO_TIMES,PA,PREVIEW_NETWORKS)));
        
        publisherUpdaters.put(BBC_REDUX, resultHandlingUpdater(new RootEquivalenceUpdater(containerUpdaterBuilder(Sets.union(acceptablePublishers, ImmutableSet.of(BBC_REDUX)))
                .withGenerators(ImmutableSet.of(
                        titleScoringGenerator,
                        new ContainerChildEquivalenceGenerator(contentResolver, itemUpdater, liveResultsStore(), log)))
                .withScorer(titleScoringGenerator)
                .build(), itemUpdater),Sets.union(acceptablePublishers, ImmutableSet.of(BBC_REDUX)))
        );
        
        publisherUpdaters.put(ITUNES, resultHandlingUpdater(new RootEquivalenceUpdater(
                containerUpdaterBuilder(acceptablePublishers)
                    .withGenerator(titleScoringGenerator)
                    .withScorers(ImmutableSet.of(titleScoringGenerator, 
                            new ContainerChildEquivalenceScorer(itemUpdater, liveResultsStore(), contentResolver, log),
                            new ContainerHierarchyMatchingEquivalenceScorer(contentResolver)))
                    .build(), 
                itemUpdater),acceptablePublishers));
        
        publisherUpdaters.put(FACEBOOK, resultHandlingUpdater(new RootEquivalenceUpdater(
            containerUpdaterBuilder(Sets.union(acceptablePublishers, ImmutableSet.of(FACEBOOK)))
                .withResultBuilder(DefaultEquivalenceResultBuilder.<Container>resultBuilder(
                    NullScoreAwareAveragingCombiner.<Container>get(),
                    new SpecializationMatchingEquivalenceExtractor<Container>(
                        new MinimumScoreEquivalenceExtractor<Container>(
                            PercentThresholdEquivalenceExtractor.<Container>moreThanPercent(90), 
                            0.2
                        )
                    )
                ))
                .withGenerators(ImmutableSet.of(
                    titleScoringGenerator,
                    aliasResolvingGenerator(contentResolver, Container.class)
                ))
                .build(),
            NullEquivalenceUpdater.<Item>get()
        ), Sets.union(acceptablePublishers, ImmutableSet.of(FACEBOOK))));
        
        return new PublisherSwitchingContentEquivalenceUpdater(publisherUpdaters.build());
    }
    
}
