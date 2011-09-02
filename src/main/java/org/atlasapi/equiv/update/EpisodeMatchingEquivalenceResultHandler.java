package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.Maps;

public class EpisodeMatchingEquivalenceResultHandler implements EquivalenceResultHandler<Item> {

    private final EquivalenceResultHandler<Item> delegate;
    private List<List<Episode>> strongContainerChildren;

    public EpisodeMatchingEquivalenceResultHandler(EquivalenceResultHandler<Item> delegate, List<List<Episode>> strongContainerChildren) {
        this.delegate = delegate;
        this.strongContainerChildren = strongContainerChildren;
    }
    
    @Override
    public void handle(EquivalenceResult<Item> result) {
        Episode target = (Episode) result.target();
        if (target.getEpisodeNumber() != null && target.getSeriesNumber() != null) {
            
            List<ScoredEquivalents<Item>> rawScores = result.rawScores();

            String combinedSource = result.combinedEquivalences().source();
            Map<Item, Score> combinedEquivalences = Maps.newHashMap(result.combinedEquivalences().equivalents());
            Map<Publisher, ScoredEquivalent<Item>> strongEquivalences = Maps.newHashMap(result.strongEquivalences());

            stitch(target, combinedEquivalences, strongEquivalences);
            result = new EquivalenceResult<Item>(target, rawScores, DefaultScoredEquivalents.fromMappedEquivs(combinedSource, combinedEquivalences), strongEquivalences);
            
        }
        delegate.handle(result);
    }

    private void stitch(Episode target, Map<Item, Score> combinedEquivalences, Map<Publisher, ScoredEquivalent<Item>> strongEquivalences) {
        for (List<Episode> childList : strongContainerChildren) {
            for (Episode ep : childList) {
                if(matchingSequenceNumbers(target, ep) && strongEquivalences.get(ep.getPublisher()) == null) {
                    if(!combinedEquivalences.containsKey(ep)) {
                        combinedEquivalences.put(ep, Score.valueOf(2.0));
                    }
                    strongEquivalences.put(ep.getPublisher(), ScoredEquivalent.<Item>equivalentScore(ep, Score.valueOf(2.0)));
                    break;
                }
            }
        }
    }

    public boolean matchingSequenceNumbers(Episode target, Episode ep) {
        return target.getEpisodeNumber().equals(ep.getEpisodeNumber()) && target.getSeriesNumber().equals(ep.getSeriesNumber());
    }
    
}
