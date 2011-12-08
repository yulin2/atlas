package org.atlasapi.equiv.generators;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.scores.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public class ContainerChildEquivalenceGenerator implements ContentEquivalenceGenerator<Container> {
    
    private static final int ITEM_SCORE_SCALER = 20;
    public static final String NAME = "Item";
    
    private final ContentResolver contentResolver;
    private final ContentEquivalenceUpdater<Item> itemUpdater;
    private final LiveEquivalenceResultStore resultStore;
    private final AdapterLog log;
    
    public ContainerChildEquivalenceGenerator(ContentResolver contentResolver, ContentEquivalenceUpdater<Item> itemUpdater, LiveEquivalenceResultStore resultStore, AdapterLog log) {
        this.contentResolver = contentResolver;
        this.itemUpdater = itemUpdater;
        this.resultStore = resultStore;
        this.log = log;
    }
    
    @Override
    public ScoredEquivalents<Container> generate(Container content, ResultDescription desc) {
        return extractContainersFrom(calculateContainerChildResults(content, desc), desc);
    }

    private Set<EquivalenceResult<Item>> calculateContainerChildResults(Container container, ResultDescription desc) {
        desc.startStage("Computing initial child results");
        Set<EquivalenceResult<Item>> childResults = computeInitialChildResults(Lists.transform(container.getChildRefs(), ChildRef.TO_URI));
        desc.appendText("%s child results", childResults.size()).finishStage();
        return childResults;
    }
    
   /* Resolve all children of the container and use the supplied item equivalence updater to compute equivalence results for the children.
    * Results are put into the results store for later retrieval after container equivalence has been fully computed.  
    */
    private Set<EquivalenceResult<Item>> computeInitialChildResults(List<String> childrenUris) {
        final Map<String, Identified> resolvedChildren = contentResolver.findByCanonicalUris(childrenUris).asResolvedMap();
        
        return ImmutableSet.copyOf(Iterables.filter(Iterables.transform(childrenUris, new Function<String, EquivalenceResult<Item>>() {
            @Override
            public EquivalenceResult<Item> apply(String childUri) {
                Identified child = resolvedChildren.get(childUri);
                if (child == null || !(child instanceof Item)) {
                    return null;
                }
                return resultStore.store(itemUpdater.updateEquivalences((Item)child));
            }
        }), Predicates.notNull()));
    }
    
    /* Calculates equivalence scores for the containers of items that are strongly equivalent to the items of the subject container.
     * Scores are normalized by the number of items in the container. 
     */
    private ScoredEquivalents<Container> extractContainersFrom(Set<EquivalenceResult<Item>> childResults, ResultDescription desc) {

        desc.startStage("Extracting containers from child results");
        
        //Local cache, hopefully the same containers will be resolved multiple times.
        Map<String, Maybe<Container>> containerCache = Maps.newHashMap();
        
        ScoredEquivalentsBuilder<Container> containerEquivalents = DefaultScoredEquivalents.fromSource(NAME);
        
        for (EquivalenceResult<Item> equivalenceResult : childResults) {
            for (ScoredEquivalent<Item> strongEquivalent : equivalenceResult.strongEquivalences().values()) {
                Score score = strongEquivalent.score();
                if (score.isRealScore()) {
                    ParentRef parentEquivalent = strongEquivalent.equivalent().getContainer();
                    Maybe<Container> resolvedContainer = resolve(parentEquivalent, containerCache);

                    if (resolvedContainer.hasValue()) {
                        Container container = resolvedContainer.requireValue();
                        containerEquivalents.addEquivalent(container, Score.valueOf(score.asDouble() / container.getChildRefs().size()));
                    }
                }
            }
        }
        
        //Give more weight to the scores...
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
    
    //Resolve a container, looking in the cache first.
    private Maybe<Container> resolve(ParentRef parentEquivalent, Map<String, Maybe<Container>> containerCache) {
        if(parentEquivalent == null) {
            return Maybe.nothing();
        }
        
        String uri = parentEquivalent.getUri();
        
        Maybe<Container> cached = containerCache.get(uri);
        
        if (cached != null) {
            return cached;
        }
        
        Maybe<Identified> resolved = contentResolver.findByCanonicalUris(ImmutableList.of(uri)).get(uri);
        
        if(resolved.isNothing() || !(resolved.requireValue() instanceof Container)) {
            log.record(warnEntry().withSource(getClass()).withDescription("Couldn't resolve container" + uri));
            return Maybe.nothing();
        }
        
        Maybe<Container> result = Maybe.just((Container) resolved.requireValue());
        containerCache.put(uri, result);
        
        return result;
    }
}
