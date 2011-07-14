package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EpisodeMatchingEquivalenceResultHandler implements EquivalenceResultHandler<Item> {

    private final EquivalenceResultHandler<Item> delegate;
    private final Set<Container> strongContainers;
    private List<List<Episode>> strongContainerChildren;

    public EpisodeMatchingEquivalenceResultHandler(EquivalenceResultHandler<Item> delegate, Set<Container> strongContainers, List<List<Episode>> strongContainerChildren) {
        this.delegate = delegate;
        this.strongContainers = strongContainers;
        this.strongContainerChildren = strongContainerChildren;
    }
    
    @Override
    public void handle(EquivalenceResult<Item> result) {
        
        Episode target = (Episode) result.target();
        List<ScoredEquivalents<Item>> rawScores = result.rawScores();

        String combinedSource = result.combinedEquivalences().source();
        Map<Item, Score> combinedEquivalences = Maps.newHashMap(result.combinedEquivalences().equivalents());
        Map<Publisher, ScoredEquivalent<Item>> strongEquivalences = Maps.newHashMap(filter(result.strongEquivalences()));
        
        if(target.getEpisodeNumber() != null && target.getSeriesNumber() != null) {
            stitch(target, combinedEquivalences, strongEquivalences);
        }
        
        result = new EquivalenceResult<Item>(target, rawScores, DefaultScoredEquivalents.fromMappedEquivs(combinedSource, combinedEquivalences), strongEquivalences);
        
        delegate.handle(result);
        
    }

    private void stitch(Episode target, Map<Item, Score> combinedEquivalences, Map<Publisher, ScoredEquivalent<Item>> strongEquivalences) {
        for (List<Episode> childList : strongContainerChildren) {
            for (Episode ep : childList) {
                if(target.getEpisodeNumber().equals(ep.getEpisodeNumber()) && target.getSeriesNumber().equals(ep.getSeriesNumber())) {
                    if(!combinedEquivalences.containsKey(ep)) {
                        combinedEquivalences.put(ep, Score.valueOf(2.0));
                    }
                    strongEquivalences.put(ep.getPublisher(), ScoredEquivalent.<Item>equivalentScore(ep, Score.valueOf(2.0)));
                    break;
                }
            }
        }
    }
    
    private Map<Publisher, ScoredEquivalent<Item>> filter(Map<Publisher, ScoredEquivalent<Item>> strongItems) {
        
        final ImmutableSet<String> containerRefs = ImmutableSet.copyOf(Iterables.transform(strongContainers, Identified.TO_URI));
        
        return Maps.filterValues(Maps.transformValues(strongItems, new Function<ScoredEquivalent<Item>, ScoredEquivalent<Item>>() {
            @Override
            public ScoredEquivalent<Item> apply(ScoredEquivalent<Item> input) {
                if(!containerRefs.contains(input.equivalent().getContainer().getUri())) {
                    return null;
                }
                return input;
            }
        }), Predicates.notNull());
    }

}
