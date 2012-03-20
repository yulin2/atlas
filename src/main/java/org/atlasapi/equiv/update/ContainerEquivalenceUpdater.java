package org.atlasapi.equiv.update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.generators.ContainerChildEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
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
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.content.ChildRef;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.Episode;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ContainerEquivalenceUpdater implements ContentEquivalenceUpdater<Container> {

    public static class Builder {

        private final ContentResolver contentResolver;
        private final LiveEquivalenceResultStore resultStore;
        private final EquivalenceResultBuilder<Container> containerResultBuilder;
        private final EquivalenceResultHandler<Item> itemResultHandler;
        private final AdapterLog log; 
        
        private Iterable<ContentEquivalenceGenerator<Container>> generators; 
        private Iterable<ContentEquivalenceScorer<Container>> scorers;

        public Builder(ContentResolver contentResolver, LiveEquivalenceResultStore resultStore, 
                EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultHandler<Item> itemResultHandler, AdapterLog log) {
                    this.contentResolver = contentResolver;
                    this.resultStore = resultStore;
                    this.containerResultBuilder = containerResultBuilder;
                    this.itemResultHandler = itemResultHandler;
                    this.log = log;
        }
     
        public Builder withGenerator(ContentEquivalenceGenerator<Container> generator) {
            this.generators = ImmutableSet.of(generator);
            return this;
        }
        
        public Builder withGenerators(Iterable<ContentEquivalenceGenerator<Container>> generators) {
            this.generators = generators;
            return this;
        }
        
        public Builder withScorer(ContentEquivalenceScorer<Container> scorer) {
            this.scorers = ImmutableSet.of(scorer);
            return this;
        }
        
        public Builder withScorers(Iterable<ContentEquivalenceScorer<Container>> scorers) {
            this.scorers = scorers;
            return this;
        }
        
        public ContainerEquivalenceUpdater build() {
            EquivalenceGenerators<Container> generatorSet = new EquivalenceGenerators<Container>(ImmutableSet.copyOf(generators), log);
            EquivalenceScorers<Container> scorerSet = new EquivalenceScorers<Container>(ImmutableSet.copyOf(scorers), log);
            return new ContainerEquivalenceUpdater(contentResolver, resultStore, containerResultBuilder, itemResultHandler, generatorSet, scorerSet);
        }
    }
    
    public static Builder containerUpdater(ContentResolver contentResolver, LiveEquivalenceResultStore resultStore, 
            EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultHandler<Item> itemResultHandler, AdapterLog log) {
        return new Builder(contentResolver, resultStore, containerResultBuilder, itemResultHandler, log);
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
        Map<String,ScoredEquivalents<Container>> generatedEquivalences = Maps.uniqueIndex(generators.generate(content, desc), ScoredEquivalents.TO_SOURCE);
        
        Set<Container> extractGeneratedSuggestions = extractGeneratedSuggestions(generatedEquivalences.values());
        
        //ensure default (0) item score for all containers. 
        ScoredEquivalents<Container> itemGeneratorScores = generatedEquivalences.get(ITEM_UPDATER_NAME);
        if (itemGeneratorScores != null) {
            generatedEquivalences = Maps.newHashMap(generatedEquivalences);
            generatedEquivalences.put(ITEM_UPDATER_NAME, addZeros(extractGeneratedSuggestions, itemGeneratorScores));
        }
        
        //score all generated suggestions
        List<ScoredEquivalents<Container>> scoredEquivalents = scorers.score(content, ImmutableList.copyOf(extractGeneratedSuggestions), desc);
        
        //build container result.
        EquivalenceResult<Container> containerResult = containerResultBuilder.resultFor(content, merger.merge(ImmutableList.copyOf(generatedEquivalences.values()), scoredEquivalents), desc);
        
        //strongly equivalent containers;
        Set<Container> strongContainers = ImmutableSet.copyOf(Iterables.transform(containerResult.strongEquivalences().values(), ScoredEquivalent.<Container>toEquivalent()));

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
    
    private ScoredEquivalents<Container> addZeros(Iterable<Container> extractGeneratedSuggestions, ScoredEquivalents<Container> strongItemContainers) {
        
        HashMap<Container, Score> current = Maps.newHashMap(strongItemContainers.equivalents());
        for (Container container : extractGeneratedSuggestions) {
            if(!current.containsKey(container)) {
                current.put(container, Score.valueOf(0.0));
            }
        }
        
        return DefaultScoredEquivalents.fromMappedEquivs(ITEM_UPDATER_NAME, current);
    }

    private Set<Container> extractGeneratedSuggestions(Iterable<ScoredEquivalents<Container>> generatedScores) {
        return Sets.newHashSet(Iterables.concat(Iterables.transform(generatedScores, new Function<ScoredEquivalents<Container>, Iterable<Container>>() {
            @Override
            public Iterable<Container> apply(ScoredEquivalents<Container> input) {
                return input.equivalents().keySet();
            }
        })));
    }

}
