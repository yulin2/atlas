package org.atlasapi.equiv.update;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

public class ContainerEquivalenceUpdater implements ContentEquivalenceUpdater<Container> {

    private static final int ITEM_SCORE_SCALER = 20;
    public static final String ITEM_UPDATER = "Item";

    private final ContentResolver contentResolver;
    private final AdapterLog log;

    private final ItemEquivalenceUpdater<Item> itemUpdater;
    
    private EquivalenceGenerators<Container> generators;
    private EquivalenceScorers<Container> scorers;
    
    private final EquivalenceResultBuilder<Container> containerResultBuilder;
    private final EquivalenceResultHandler<Item> itemResultHandler;
    
    private final ScoredEquivalentsMerger merger = new ScoredEquivalentsMerger();

    public ContainerEquivalenceUpdater(ContentResolver contentResolver, ItemEquivalenceUpdater<Item> itemUpdater, 
            EquivalenceResultBuilder<Container> containerResultBuilder, EquivalenceResultHandler<Item> itemResultHandler, AdapterLog log) {
                this.contentResolver = contentResolver;
                this.itemUpdater = itemUpdater;
                this.containerResultBuilder = containerResultBuilder;
                this.itemResultHandler = itemResultHandler;
                this.log = log;
    }

    public ContainerEquivalenceUpdater withEquivalenceGenerators(EquivalenceGenerators<Container> generators) {
        this.generators = generators;
        return this;
    }

    public ContainerEquivalenceUpdater withEquivalenceScorers(EquivalenceScorers<Container> scorers) {
        this.scorers = scorers;
        return this;
    }

    @Override
    public EquivalenceResult<Container> updateEquivalences(Container content) {
        
        Map<String, Container> containerCache = Maps.newHashMap(); //local cache.
        
        List<String> childrenUris = Lists.transform(content.getChildRefs(), ChildRef.TO_URI);
        
        ReadableDescription desc = new DefaultDescription();

        // compute results for container children.
        desc.startStage("Computing initial child results");
        Set<EquivalenceResult<Item>> childResults = computeInitialChildResults(childrenUris, content.getCanonicalUri());
        desc.appendText("%s child results", childResults.size()).finishStage();
        
        //containers suggested by items
        ScoredEquivalents<Container> strongItemContainers = extractContainersFrom(childResults, childrenUris.size(), containerCache, desc);
        
        //generate other container equivalents.
        List<ScoredEquivalents<Container>> generatedEquivalences = generators.generate(content, desc);
        
        Set<Container> extractGeneratedSuggestions = extractGeneratedSuggestions(generatedEquivalences);
        
        //ensure default (0) item score for all containers. 
        strongItemContainers = addZeros(extractGeneratedSuggestions, strongItemContainers);
        
        generatedEquivalences.add(strongItemContainers);
        extractGeneratedSuggestions.addAll(extractGeneratedSuggestions(ImmutableList.of(strongItemContainers)));
        
        //score all generated suggestions
        List<ScoredEquivalents<Container>> scoredEquivalents = scorers.score(content, ImmutableList.copyOf(extractGeneratedSuggestions), desc);
        
        //build container result.
        EquivalenceResult<Container> containerResult = containerResultBuilder.resultFor(content, merger.merge(generatedEquivalences, scoredEquivalents), desc);
        
        //containerResult = filterNonPositiveItemScores(strongItemContainers, containerResult);
        
        //strongly equivalent containers;
        Set<Container> strongContainers = ImmutableSet.copyOf(Iterables.transform(containerResult.strongEquivalences().values(), ScoredEquivalent.<Container>toEquivalent()));

        ImmutableList<List<Episode>> strongContainerChildren = ImmutableList.copyOf(Iterables.transform(strongContainers, new Function<Container, List<Episode>>() {
            @Override
            public List<Episode> apply(Container input) {
                ResolvedContent resolvedChildRefs = contentResolver.findByCanonicalUris(Iterables.transform(input.getChildRefs(), ChildRef.TO_URI));
                return ImmutableList.copyOf(Iterables.filter(resolvedChildRefs.getAllResolvedResults(), Episode.class));
            }
        }));
        
        EquivalenceResultHandler<Item> episodeMatchingHandler = new EpisodeFilteringEquivalenceResultHandler(new EpisodeMatchingEquivalenceResultHandler(itemResultHandler, strongContainerChildren), strongContainers) ;

        for (EquivalenceResult<Item> equivalenceResult : childResults) {
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
        
        return DefaultScoredEquivalents.fromMappedEquivs(strongItemContainers.source(), current);
    }

    private Set<Container> extractGeneratedSuggestions(Iterable<ScoredEquivalents<Container>> generatedScores) {
        return Sets.newHashSet(Iterables.concat(Iterables.transform(generatedScores, new Function<ScoredEquivalents<Container>, Iterable<Container>>() {
            @Override
            public Iterable<Container> apply(ScoredEquivalents<Container> input) {
                return input.equivalents().keySet();
            }
        })));
    }

    private ScoredEquivalents<Container> extractContainersFrom(Set<EquivalenceResult<Item>> childResults, int children, Map<String, Container> containerCache, ResultDescription desc) {

        desc.startStage("Extracting containers from child results");
        ScoredEquivalentsBuilder<Container> containerEquivalents = DefaultScoredEquivalents.fromSource(ITEM_UPDATER);
        
        for (EquivalenceResult<Item> equivalenceResult : childResults) {
            for (ScoredEquivalent<Item> strongEquivalent : equivalenceResult.strongEquivalences().values()) {
                
                ParentRef parentEquivalent = strongEquivalent.equivalent().getContainer();
                Container container = resolve(parentEquivalent, containerCache);
                
                if (container != null) {
                    Score score = strongEquivalent.score();
                    if(score.isRealScore()) {
                        score = Score.valueOf(score.asDouble() / container.getChildRefs().size());
                    }
                    containerEquivalents.addEquivalent(container, score);
                }
                
            }
        }
        
        ScaledScoredEquivalents<Container> scaled = ScaledScoredEquivalents.scale(containerEquivalents.build(), new Function<Double, Double>() {
            @Override
            public Double apply(Double input) {
                return Math.min(1, input * ITEM_SCORE_SCALER);
            }
        });
        
        for (Entry<Container, Score> result : scaled.equivalents().entrySet()) {
            desc.appendText("%s (%s) scored %s", result.getKey().getTitle(), result.getKey().getCanonicalUri(), result.getValue());
        }
        desc.finishStage();
        
        return scaled;
    }
    
    private Container resolve(ParentRef parentEquivalent, Map<String, Container> containerCache) {
        if(parentEquivalent == null) {
            return null;
        }
        
        //TODO make this a CachingContentResolver?
        String uri = parentEquivalent.getUri();
        if(containerCache.containsKey(uri)) {
            return containerCache.get(uri);
        }

        Maybe<Identified> resolved = contentResolver.findByCanonicalUris(ImmutableList.of(uri)).get(uri);
        
        if(resolved.isNothing() || !(resolved.requireValue() instanceof Container)) {
            log.record(warnEntry().withSource(getClass()).withDescription("Couldn't resolve container" + uri));
            return null;
        }
        
        Container requireValue = (Container) resolved.requireValue();
        containerCache.put(uri, requireValue);
        
        return requireValue;
    }

    private Set<EquivalenceResult<Item>> computeInitialChildResults(List<String> childrenUris, String contentUri) {
        Map<String, Identified> resolvedChildren = contentResolver.findByCanonicalUris(childrenUris).asResolvedMap();
        
        Set<EquivalenceResult<Item>> childResults = Sets.newHashSet();
        
        for (String childUri : childrenUris) {
            Identified child = resolvedChildren.get(childUri);
            if(child instanceof Item) {
                EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences((Item)child);
                childResults.add(itemEquivalences);
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("Resolved %s child %s to %s not Item", contentUri, childUri, child.getClass().getSimpleName()));
            }
        }
        return childResults;
    }
}
