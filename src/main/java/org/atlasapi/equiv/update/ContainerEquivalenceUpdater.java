package org.atlasapi.equiv.update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.generators.ContainerChildEquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.handlers.EpisodeFilteringEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EpisodeMatchingEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.EquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ContainerEquivalenceUpdater implements EquivalenceUpdater<Container> {

    public static class Builder {

        private final ContentResolver contentResolver;
        private final LiveEquivalenceResultStore resultStore;
        private final EquivalenceResultHandler<Item> itemResultHandler;
        
        private EquivalenceResultBuilder<Container> containerResultBuilder;
        private Iterable<EquivalenceGenerator<Container>> generators = ImmutableSet.of(); 
        private Iterable<EquivalenceScorer<Container>> scorers = ImmutableSet.of();

        public Builder(ContentResolver contentResolver, LiveEquivalenceResultStore resultStore, 
                EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultHandler<Item> itemResultHandler) {
                    this.contentResolver = contentResolver;
                    this.resultStore = resultStore;
                    this.containerResultBuilder = containerResultBuilder;
                    this.itemResultHandler = itemResultHandler;
        }
        
        public Builder withResultBuilder(EquivalenceResultBuilder<Container> builder) {
            this.containerResultBuilder = builder;
            return this;
        }
     
        public Builder withGenerator(EquivalenceGenerator<Container> generator) {
            this.generators = ImmutableSet.of(generator);
            return this;
        }
        
        public Builder withGenerators(Iterable<EquivalenceGenerator<Container>> generators) {
            this.generators = generators;
            return this;
        }
        
        public Builder withScorer(EquivalenceScorer<Container> scorer) {
            this.scorers = ImmutableSet.of(scorer);
            return this;
        }
        
        public Builder withScorers(Iterable<EquivalenceScorer<Container>> scorers) {
            this.scorers = scorers;
            return this;
        }
        
        public ContainerEquivalenceUpdater build() {
            EquivalenceGenerators<Container> generatorSet = new EquivalenceGenerators<Container>(ImmutableSet.copyOf(generators));
            EquivalenceScorers<Container> scorerSet = new EquivalenceScorers<Container>(ImmutableSet.copyOf(scorers));
            return new ContainerEquivalenceUpdater(contentResolver, resultStore, containerResultBuilder, itemResultHandler, generatorSet, scorerSet);
        }
    }
    
    public static Builder containerUpdater(ContentResolver contentResolver, LiveEquivalenceResultStore resultStore, 
            EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultHandler<Item> itemResultHandler) {
        return new Builder(contentResolver, resultStore, containerResultBuilder, itemResultHandler);
    }
    
    public static final String ITEM_UPDATER_NAME = ContainerChildEquivalenceGenerator.NAME;

    private final ContentResolver contentResolver;

    private final EquivalenceGenerators<Container> generators;
    private final EquivalenceScorers<Container> scorers;
    
    private final EquivalenceResultBuilder<Container> containerResultBuilder;
    private final EquivalenceResultHandler<Item> itemResultHandler;
    
    private final ScoredEquivalentsMerger merger = new ScoredEquivalentsMerger();
    private final LiveEquivalenceResultStore resultStore;

    public ContainerEquivalenceUpdater(ContentResolver contentResolver, LiveEquivalenceResultStore resultStore, 
            EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultHandler<Item> itemResultHandler, 
            EquivalenceGenerators<Container> generators, EquivalenceScorers<Container> scorers) {
                this.contentResolver = contentResolver;
                this.resultStore = resultStore;
                this.containerResultBuilder = containerResultBuilder;
                this.itemResultHandler = itemResultHandler;
                this.generators = generators;
                this.scorers = scorers;
    }

    @Override
    public EquivalenceResult<Container> updateEquivalences(Container content, Optional<List<Container>> externalCandidates) {
        
        ReadableDescription desc = new DefaultDescription();
        
        //generate container equivalents.
        Map<String,ScoredCandidates<Container>> generatedEquivalences = Maps.uniqueIndex(generators.generate(content, desc), ScoredCandidates.TO_SOURCE);
        
        Set<Container> extractGeneratedSuggestions = extractGeneratedSuggestions(generatedEquivalences.values());
        
        //ensure default (0) item score for all containers. 
        ScoredCandidates<Container> itemGeneratorScores = generatedEquivalences.get(ITEM_UPDATER_NAME);
        if (itemGeneratorScores != null) {
            generatedEquivalences = Maps.newHashMap(generatedEquivalences);
            generatedEquivalences.put(ITEM_UPDATER_NAME, addZeros(extractGeneratedSuggestions, itemGeneratorScores));
        }
        
        //score all generated suggestions
        List<ScoredCandidates<Container>> scoredEquivalents = scorers.score(content, ImmutableList.copyOf(extractGeneratedSuggestions), desc);
        
        //build container result.
        EquivalenceResult<Container> containerResult = containerResultBuilder.resultFor(content, merger.merge(ImmutableList.copyOf(generatedEquivalences.values()), scoredEquivalents), desc);
        
        //strongly equivalent containers;
        Set<Container> strongContainers = ImmutableSet.copyOf(Iterables.transform(containerResult.strongEquivalences().values(), ScoredCandidate.<Container>toEquivalent()));

        ImmutableList<List<Episode>> strongContainerChildren = ImmutableList.copyOf(Iterables.transform(strongContainers, new Function<Container, List<Episode>>() {
            @Override
            public List<Episode> apply(Container input) {
                ResolvedContent resolvedChildRefs = contentResolver.findByCanonicalUris(Iterables.transform(input.getChildRefs(), ChildRef.TO_URI));
                return ImmutableList.copyOf(Iterables.filter(resolvedChildRefs.getAllResolvedResults(), Episode.class));
            }
        }));
        
        EquivalenceResultHandler<Item> episodeMatchingHandler = new EpisodeMatchingEquivalenceResultHandler(itemResultHandler, strongContainerChildren);
        episodeMatchingHandler = new EpisodeFilteringEquivalenceResultHandler(episodeMatchingHandler, strongContainers);

        for (EquivalenceResult<Item> equivalenceResult : resultStore.resultsFor(Lists.transform(content.getChildRefs(), ChildRef.TO_URI))) {
            episodeMatchingHandler.handle(equivalenceResult);
        }
        
        return containerResult;
    }
    
    private ScoredCandidates<Container> addZeros(Iterable<Container> extractGeneratedSuggestions, ScoredCandidates<Container> strongItemContainers) {
        
        HashMap<Container, Score> current = Maps.newHashMap(strongItemContainers.candidates());
        for (Container container : extractGeneratedSuggestions) {
            if(!current.containsKey(container)) {
                current.put(container, Score.valueOf(0.0));
            }
        }
        
        return DefaultScoredEquivalents.fromMappedEquivs(ITEM_UPDATER_NAME, current);
    }

    private Set<Container> extractGeneratedSuggestions(Iterable<ScoredCandidates<Container>> generatedScores) {
        return Sets.newHashSet(Iterables.concat(Iterables.transform(generatedScores, new Function<ScoredCandidates<Container>, Iterable<Container>>() {
            @Override
            public Iterable<Container> apply(ScoredCandidates<Container> input) {
                return input.candidates().keySet();
            }
        })));
    }

}
