package org.atlasapi.equiv.generators;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.persistence.LiveEquivalenceResultStore;
import org.atlasapi.equiv.results.scores.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.update.EquivalenceUpdater;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ContainerChildEquivalenceGenerator implements EquivalenceGenerator<Container> {
    
    private static final int ITEM_SCORE_SCALER = 20;
    public static final String NAME = "Item";
    
    private final ContentResolver contentResolver;
    private final EquivalenceUpdater<Item> itemUpdater;
    private final LiveEquivalenceResultStore resultStore;
    private ItemResultContainerResolver itemResultContainerResolver;
    
    public ContainerChildEquivalenceGenerator(ContentResolver contentResolver, EquivalenceUpdater<Item> itemUpdater, LiveEquivalenceResultStore resultStore) {
        this.contentResolver = contentResolver;
        this.itemUpdater = itemUpdater;
        this.resultStore = resultStore;
        this.itemResultContainerResolver = new ItemResultContainerResolver(contentResolver, NAME);
    }
    
    @Override
    public ScoredCandidates<Container> generate(Container content, ResultDescription desc) {
        return extractContainersFrom(calculateContainerChildResults(content, desc), desc);
    }

    private Set<EquivalenceResult<Item>> calculateContainerChildResults(Container container, ResultDescription desc) {
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
                return resultStore.store(itemUpdater.updateEquivalences((Item)child, Optional.<List<Item>>absent()));
            }
        }), Predicates.notNull()));
    }
    
    /* Calculates equivalence scores for the containers of items that are strongly equivalent to the items of the subject container.
     * Scores are normalized by the number of items in the container. 
     */
    private ScoredCandidates<Container> extractContainersFrom(Set<EquivalenceResult<Item>> childResults, ResultDescription desc) {

        desc.startStage("Extracting containers from child results");
        
        ScoredCandidates<Container> containerScores = itemResultContainerResolver.extractContainersFrom(childResults);
        
        ScaledScoredEquivalents<Container> scaled = ScaledScoredEquivalents.scale(containerScores, new Function<Double, Double>() {
            @Override
            public Double apply(Double input) {
                return Math.min(1, input * ITEM_SCORE_SCALER);
            }
        });
        
        for (Entry<Container, Score> result : scaled.candidates().entrySet()) {
            desc.appendText("%s (%s) scored %s", result.getKey().getTitle(), result.getKey().getCanonicalUri(), result.getValue());
        }
        desc.finishStage();
        
        return scaled;
    }
    
    @Override
    public String toString() {
        return "Container Child Result generator";
    }
}
