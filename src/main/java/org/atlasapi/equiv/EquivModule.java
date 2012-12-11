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
import static org.atlasapi.media.entity.Publisher.BBC_REDUX;
import static org.atlasapi.media.entity.Publisher.FACEBOOK;
import static org.atlasapi.media.entity.Publisher.ITUNES;
import static org.atlasapi.media.entity.Publisher.LOVEFILM;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.atlasapi.media.entity.Publisher.PREVIEW_NETWORKS;
import static org.atlasapi.media.entity.Publisher.RADIO_TIMES;
import static org.atlasapi.media.entity.Publisher.NETFLIX;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.YOUVIEW;
import static org.atlasapi.persistence.lookup.TransitiveLookupWriter.generatedTransitiveLookupWriter;

import java.io.File;
import java.util.Set;

import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
import org.atlasapi.equiv.generators.CandidateContainerEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContainerChildEquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerator;
import org.atlasapi.equiv.generators.FilmEquivalenceGenerator;
import org.atlasapi.equiv.generators.RadioTimesFilmEquivalenceGenerator;
import org.atlasapi.equiv.generators.ScalingEquivalenceGenerator;
import org.atlasapi.equiv.generators.TitleSearchGenerator;
import org.atlasapi.equiv.generators.SongTitleTransform;
import org.atlasapi.equiv.handlers.BroadcastingEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EpisodeFilteringEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EpisodeMatchingEquivalenceHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceSummaryWritingHandler;
import org.atlasapi.equiv.handlers.LookupWritingEquivalenceHandler;
import org.atlasapi.equiv.handlers.ResultWritingEquivalenceHandler;
import org.atlasapi.equiv.results.combining.ItemScoreFilteringCombiner;
import org.atlasapi.equiv.results.combining.NullScoreAwareAveragingCombiner;
import org.atlasapi.equiv.results.combining.ScoreCombiner;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.MusicEquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor;
import org.atlasapi.equiv.results.filters.AbstractEquivalenceFilter;
import org.atlasapi.equiv.results.filters.AlwaysTrueFilter;
import org.atlasapi.equiv.results.filters.ConjunctiveFilter;
import org.atlasapi.equiv.results.filters.EquivalenceFilter;
import org.atlasapi.equiv.results.filters.MinimumScoreFilter;
import org.atlasapi.equiv.results.filters.PublisherFilter;
import org.atlasapi.equiv.results.filters.SpecializationMatchingFilter;
import org.atlasapi.equiv.results.persistence.FileEquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
import org.atlasapi.equiv.scorers.ContainerHierarchyMatchingEquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorer;
import org.atlasapi.equiv.scorers.CrewMemberScorer;
import org.atlasapi.equiv.scorers.SequenceItemEquivalenceScorer;
import org.atlasapi.equiv.scorers.TitleMatchingContainerScorer;
import org.atlasapi.equiv.scorers.TitleMatchingItemScorer;
import org.atlasapi.equiv.scorers.SongCrewMemberExtractor;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.equiv.update.EquivalenceUpdaters;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Song;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

@Configuration
public class EquivModule {

	private @Value("${equiv.results.directory}") String equivResultsDirectory;
    
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired SearchResolver searchResolver;
    private @Autowired ContentResolver contentResolver;
    private @Autowired ChannelResolver channelResolver;
    private @Autowired EquivalenceSummaryStore equivSummaryStore;
    private @Autowired LookupEntryStore lookupStore;

    public @Bean RecentEquivalenceResultStore equivalenceResultStore() {
        return new RecentEquivalenceResultStore(new FileEquivalenceResultStore(new File(equivResultsDirectory)));
    }

    private EquivalenceResultHandler<Item> itemResultHandlers(Iterable<Publisher> publishers) {
        return new BroadcastingEquivalenceResultHandler<Item>(ImmutableList.of(
            new EpisodeFilteringEquivalenceResultHandler(
                new LookupWritingEquivalenceHandler<Item>(generatedTransitiveLookupWriter(lookupStore), publishers),
                equivSummaryStore
            ),
            new ResultWritingEquivalenceHandler<Item>(equivalenceResultStore()),
            new EquivalenceSummaryWritingHandler<Item>(equivSummaryStore)
        ));
    }

    private EquivalenceResultHandler<Container> containerResultHandlers(Iterable<Publisher> publishers) {
        return new BroadcastingEquivalenceResultHandler<Container>(ImmutableList.of(
            new LookupWritingEquivalenceHandler<Container>(generatedTransitiveLookupWriter(lookupStore), publishers),
            new EpisodeMatchingEquivalenceHandler(contentResolver, equivSummaryStore, generatedTransitiveLookupWriter(lookupStore), publishers),
            new ResultWritingEquivalenceHandler<Container>(equivalenceResultStore()),
            new EquivalenceSummaryWritingHandler<Container>(equivSummaryStore)
        ));
    }
    
    private EquivalenceUpdater<Item> standardItemUpdater(Set<Publisher> acceptablePublishers) {
        Set<EquivalenceGenerator<Item>> generators = ImmutableSet.<EquivalenceGenerator<Item>>of(
                new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, channelResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(10))
        );
        Set<EquivalenceScorer<Item>> scorers = ImmutableSet.of(
                new TitleMatchingItemScorer(),
                new SequenceItemEquivalenceScorer()
        );
        return standardItemUpdater(acceptablePublishers, generators, scorers);
    }
    
    private EquivalenceUpdater<Item> standardItemUpdater(Set<Publisher> acceptablePublishers, Set<? extends EquivalenceGenerator<Item>> generators, Set<? extends EquivalenceScorer<Item>> scorers) {
        return standardContentUpdater(acceptablePublishers, generators, scorers, itemResultHandlers(acceptablePublishers));
    }

    private EquivalenceUpdater<Container> standardContainerUpdater(Set<Publisher> acceptablePublishers) {
        Set<EquivalenceGenerator<Container>> generators = ImmutableSet.of(
            TitleSearchGenerator.create(searchResolver, Container.class, acceptablePublishers),
            ScalingEquivalenceGenerator.scale(new ContainerChildEquivalenceGenerator(contentResolver, equivSummaryStore),20)
        );
        Set<EquivalenceScorer<Container>> scorers = ImmutableSet.<EquivalenceScorer<Container>>of(
            new TitleMatchingContainerScorer()
        );
        return standardContainerUpdater(acceptablePublishers, generators, scorers);
    }

    private EquivalenceUpdater<Container> standardContainerUpdater(Set<Publisher> acceptablePublishers, Set<? extends EquivalenceGenerator<Container>> generators, Set<? extends EquivalenceScorer<Container>> scorers) {
        return standardContentUpdater(acceptablePublishers, generators, scorers, containerResultHandlers(acceptablePublishers));
    }

    private <T extends Content> EquivalenceUpdater<T> standardContentUpdater(Set<Publisher> acceptablePublishers, Set<? extends EquivalenceGenerator<T>> generators, Set<? extends EquivalenceScorer<T>> scorers, EquivalenceResultHandler<T> handlers) {
        ScoreCombiner<T> combiner = new ItemScoreFilteringCombiner<T>(new NullScoreAwareAveragingCombiner<T>(), ContainerChildEquivalenceGenerator.NAME);
        ImmutableList<AbstractEquivalenceFilter<T>> filters = ImmutableList.of(
            new MinimumScoreFilter<T>(0.2),
            new SpecializationMatchingFilter<T>(),
            new PublisherFilter<T>()
        );
        return standardContentUpdater(acceptablePublishers, generators, scorers, combiner, filters, handlers);
    }

    private <T extends Content> EquivalenceUpdater<T> standardContentUpdater(Set<Publisher> acceptablePublishers, Set<? extends EquivalenceGenerator<T>> generators, Set<? extends EquivalenceScorer<T>> scorers, ScoreCombiner<T> combiner, ImmutableList<AbstractEquivalenceFilter<T>> filters, EquivalenceResultHandler<T> handler) {
        EquivalenceFilter<T> filter = new ConjunctiveFilter<T>(filters);
        EquivalenceExtractor<T> extractor = PercentThresholdEquivalenceExtractor.moreThanPercent(90);
        return new ContentEquivalenceUpdater<T>(generators, scorers, combiner, filter, extractor, handler);
    }
    
    @Bean 
    public EquivalenceUpdater<Content> contentUpdater() {
        
        Set<Publisher> musicPublishers = ImmutableSet.of(Publisher.BBC_MUSIC, Publisher.YOUTUBE, 
                Publisher.SPOTIFY, Publisher.SOUNDCLOUD, Publisher.RDIO, Publisher.AMAZON_UK);

        //Generally acceptable publishers.
        Set<Publisher> acceptablePublishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), Sets.union(ImmutableSet.of(PREVIEW_NETWORKS, BBC_REDUX, RADIO_TIMES, LOVEFILM, NETFLIX, YOUVIEW), musicPublishers));

        EquivalenceUpdater<Item> standardItemUpdater = standardItemUpdater(acceptablePublishers);
        EquivalenceUpdater<Container> standardContainerUpdater = standardContainerUpdater(acceptablePublishers);

        ImmutableSet<Publisher> nonStandardPublishers = ImmutableSet.of(ITUNES, BBC_REDUX, RADIO_TIMES, FACEBOOK, LOVEFILM, NETFLIX, YOUVIEW);
        final EquivalenceUpdaters updaters = new EquivalenceUpdaters();

        for (Publisher publisher : Iterables.filter(ImmutableList.copyOf(Publisher.values()), not(in(nonStandardPublishers)))) {
            updaters.register(publisher, Item.class, standardItemUpdater);    
            updaters.register(publisher, Container.class, standardContainerUpdater);
        }
        
        updaters.register(RADIO_TIMES, Item.class, standardItemUpdater(ImmutableSet.of(RADIO_TIMES,PA,PREVIEW_NETWORKS), ImmutableSet.of(
            new RadioTimesFilmEquivalenceGenerator(contentResolver),
            new FilmEquivalenceGenerator(searchResolver)
        ), ImmutableSet.<EquivalenceScorer<Item>>of()));
        
        EquivalenceScorer<Container> titleScorer = new TitleMatchingContainerScorer();
        SetView<Publisher> reduxPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(BBC_REDUX));
        updaters.register(BBC_REDUX, Item.class, standardItemUpdater(reduxPublishers));
        updaters.register(BBC_REDUX, Container.class, standardContainerUpdater(
            reduxPublishers,
            ImmutableSet.of(
                TitleSearchGenerator.create(searchResolver, Container.class, reduxPublishers),
                new ContainerChildEquivalenceGenerator(contentResolver, equivSummaryStore)
            ),
            ImmutableSet.of(titleScorer)
        ));
        
        SetView<Publisher> youViewPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(YOUVIEW));
        updaters.register(YOUVIEW, Item.class, standardItemUpdater(youViewPublishers));
        updaters.register(YOUVIEW, Container.class, standardContainerUpdater(
                youViewPublishers,
            ImmutableSet.of(
                TitleSearchGenerator.create(searchResolver, Container.class, youViewPublishers),
                new ContainerChildEquivalenceGenerator(contentResolver, equivSummaryStore)
            ),
            ImmutableSet.of(titleScorer)
        ));        
        
        updaters.register(ITUNES, Item.class, standardItemUpdater(
            acceptablePublishers,
            ImmutableSet.<EquivalenceGenerator<Item>>of(
                new CandidateContainerEquivalenceGenerator(contentResolver, equivSummaryStore)
            ), 
            ImmutableSet.of(
                new TitleMatchingItemScorer(),
                new SequenceItemEquivalenceScorer()
            )
        ));
        updaters.register(ITUNES, Container.class, standardContainerUpdater(
            acceptablePublishers,
            ImmutableSet.of(TitleSearchGenerator.create(searchResolver, Container.class, acceptablePublishers)), 
            ImmutableSet.of(titleScorer, 
                new ContainerHierarchyMatchingEquivalenceScorer(contentResolver)
            ))
        );
        
        Set<Publisher> lfPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(LOVEFILM));
        updaters.register(LOVEFILM, Item.class, standardItemUpdater(
            lfPublishers,
            ImmutableSet.<EquivalenceGenerator<Item>>of(
                new CandidateContainerEquivalenceGenerator(contentResolver, equivSummaryStore)
                    ), 
                    ImmutableSet.of(
                        new TitleMatchingItemScorer(),
                        new SequenceItemEquivalenceScorer()
                            )
                ));
        updaters.register(LOVEFILM, Container.class, standardContainerUpdater(
            lfPublishers,
            ImmutableSet.of(TitleSearchGenerator.create(searchResolver, Container.class, lfPublishers)), 
            ImmutableSet.of(titleScorer, 
                new ContainerHierarchyMatchingEquivalenceScorer(contentResolver)
            ))
        );
        
        Set<Publisher> netflixPublishers = Sets.union(acceptablePublishers, ImmutableSet.of(NETFLIX));
        updaters.register(NETFLIX, Item.class, standardItemUpdater(netflixPublishers, 
            ImmutableSet.<EquivalenceGenerator<Item>> of(
                new CandidateContainerEquivalenceGenerator(contentResolver, equivSummaryStore)
            ), 
            ImmutableSet.of(new TitleMatchingItemScorer(),new SequenceItemEquivalenceScorer())
        ));
        updaters.register(NETFLIX, Container.class, standardContainerUpdater(netflixPublishers,
            ImmutableSet.of(TitleSearchGenerator.create(searchResolver, Container.class, netflixPublishers)), 
            ImmutableSet.of(titleScorer, new ContainerHierarchyMatchingEquivalenceScorer(contentResolver)
            ))
        );
        
        Set<Publisher> facebookAcceptablePublishers = Sets.union(acceptablePublishers, ImmutableSet.of(FACEBOOK));
        updaters.register(FACEBOOK, Container.class, standardContentUpdater(facebookAcceptablePublishers, 
            ImmutableSet.of(
                TitleSearchGenerator.create(searchResolver, Container.class, facebookAcceptablePublishers),
                aliasResolvingGenerator(contentResolver, Container.class)
            ),
            ImmutableSet.<EquivalenceScorer<Container>>of(),
            NullScoreAwareAveragingCombiner.<Container>get(),
            ImmutableList.of(
                new MinimumScoreFilter<Container>(0.2),
                new SpecializationMatchingFilter<Container>()
            ), 
            containerResultHandlers(facebookAcceptablePublishers)
        ));
        
        for (Publisher publisher : musicPublishers) {
            updaters.register(publisher, Item.class, new ContentEquivalenceUpdater<Item>(
                ImmutableSet.<EquivalenceGenerator<Item>>of(new TitleSearchGenerator<Item>(searchResolver, Song.class, Sets.union(musicPublishers, ImmutableSet.of(ITUNES)), new SongTitleTransform(),100)), 
                ImmutableSet.<EquivalenceScorer<Item>>of(new CrewMemberScorer(new SongCrewMemberExtractor())),
                new NullScoreAwareAveragingCombiner<Item>(),
                new AlwaysTrueFilter<Item>(),
                new MusicEquivalenceExtractor(),
                itemResultHandlers(Sets.union(musicPublishers, ImmutableSet.of(Publisher.ITUNES))
            )));
        }
        
        return updaters; 
    }
}
