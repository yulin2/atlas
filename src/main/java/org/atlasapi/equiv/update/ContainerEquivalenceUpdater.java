package org.atlasapi.equiv.update;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
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
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

public class ContainerEquivalenceUpdater implements ContentEquivalenceUpdater<Container> {

    private static final int ITEM_SCORE_SCALER = 20;
    private static final String NAME = "Item";

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
        
        // compute results for container children.
        Set<EquivalenceResult<Item>> childResults = computeInitialChildResults(childrenUris, content.getCanonicalUri());
        
        //containers suggested by items
        ScoredEquivalents<Container> strongItemContainers = extractContainersFrom(childResults, childrenUris.size(), containerCache);
        
        //generate other container equivalents.
        List<ScoredEquivalents<Container>> generatedEquivalences = generators.generate(content);
        
        List<Container> extractGeneratedSuggestions = extractGeneratedSuggestions(generatedEquivalences);
        
        //ensure default (0) item score for all containers. 
        strongItemContainers = addZeros(extractGeneratedSuggestions, strongItemContainers);
        
        generatedEquivalences.add(strongItemContainers);
        extractGeneratedSuggestions.addAll(extractGeneratedSuggestions(ImmutableList.of(strongItemContainers)));
        
        //score all generated suggestions
        List<ScoredEquivalents<Container>> scoredEquivalents = scorers.score(content, extractGeneratedSuggestions);
        
        //build container result.
        EquivalenceResult<Container> containerResult = containerResultBuilder.resultFor(content, merger.merge(generatedEquivalences, scoredEquivalents));
        
        containerResult = filterNonPositiveItemScores(strongItemContainers, containerResult);
        
        //strongly equivalent containers;
        Set<Container> strongContainers = ImmutableSet.copyOf(Iterables.transform(containerResult.strongEquivalences().values(), ScoredEquivalent.<Container>toEquivalent()));

        ImmutableList<List<Episode>> strongContainerChildren = ImmutableList.copyOf(Iterables.transform(strongContainers, new Function<Container, List<Episode>>() {
            @Override
            public List<Episode> apply(Container input) {
                ResolvedContent resolvedChildRefs = contentResolver.findByCanonicalUris(Iterables.transform(input.getChildRefs(), ChildRef.TO_URI));
                return ImmutableList.copyOf(Iterables.filter(resolvedChildRefs.getAllResolvedResults(), Episode.class));
            }
        }));
        
        EpisodeMatchingEquivalenceResultHandler episodeMatchingHandler = new EpisodeMatchingEquivalenceResultHandler(itemResultHandler, strongContainers, strongContainerChildren);
        
        for (EquivalenceResult<Item> equivalenceResult : childResults) {
            episodeMatchingHandler.handle(equivalenceResult);
        }
        
        return containerResult;
    }
    
    private ScoredEquivalents<Container> addZeros(List<Container> extractGeneratedSuggestions, ScoredEquivalents<Container> strongItemContainers) {
        
        HashMap<Container, Score> current = Maps.newHashMap(strongItemContainers.equivalents());
        for (Container container : extractGeneratedSuggestions) {
            if(!current.containsKey(container)) {
                current.put(container, Score.valueOf(0.0));
            }
        }
        
        return DefaultScoredEquivalents.fromMappedEquivs(strongItemContainers.source(), current);
    }

    private EquivalenceResult<Container> filterNonPositiveItemScores(ScoredEquivalents<Container> itemScores, EquivalenceResult<Container> result) {
        
        final Map<Container, Score> itemSourceScores = itemScores.equivalents();
        
        Map<Publisher, ScoredEquivalent<Container>> strongEquivs = result.strongEquivalences();
        
        Map<Publisher, ScoredEquivalent<Container>> filterStrongs = Maps.filterValues(strongEquivs, new Predicate<ScoredEquivalent<Container>>() {
            @Override
            public boolean apply(ScoredEquivalent<Container> input) {
                Score score = itemSourceScores.get(input.equivalent());
                return score != null && score.isRealScore() && score.asDouble() > 0;
            }
        });
        
        return new EquivalenceResult<Container>(result.target(), result.rawScores(), result.combinedEquivalences(), filterStrongs);
    }

    private List<Container> extractGeneratedSuggestions(Iterable<ScoredEquivalents<Container>> generatedScores) {
        return Lists.newArrayList(Iterables.concat(Iterables.transform(generatedScores, new Function<ScoredEquivalents<Container>, Iterable<Container>>() {
            @Override
            public Iterable<Container> apply(ScoredEquivalents<Container> input) {
                return input.equivalents().keySet();
            }
        })));
    }

    private ScoredEquivalents<Container> extractContainersFrom(Set<EquivalenceResult<Item>> childResults, int children, Map<String, Container> containerCache) {

        ScoredEquivalentsBuilder<Container> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
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
        
        return ScaledScoredEquivalents.scale(containerEquivalents.build(), new Function<Double, Double>() {
            @Override
            public Double apply(Double input) {
                return Math.min(1, input * ITEM_SCORE_SCALER);
            }
        });
    }
    
    private Container resolve(ParentRef parentEquivalent, Map<String, Container> containerCache) {
        
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
            if(child instanceof Episode) {
                EquivalenceResult<Item> itemEquivalences = itemUpdater.updateEquivalences((Item)child);
                childResults.add(itemEquivalences);
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("Resolved %s child %s to null/not Item", contentUri, childUri));
            }
        }
        return childResults;
    }
}
