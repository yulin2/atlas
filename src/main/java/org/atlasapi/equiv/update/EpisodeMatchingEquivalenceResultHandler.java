package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
import org.atlasapi.equiv.results.description.ResultDescription;
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
        
        ResultDescription desc = result.description().startStage("Episode sequence stitching");
        
        List<ScoredEquivalents<Item>> rawScores = result.rawScores();

        String combinedSource = result.combinedEquivalences().source();
        Map<Item, Score> combinedEquivalences = Maps.newHashMap(result.combinedEquivalences().equivalents());
        Map<Publisher, ScoredEquivalent<Item>> strongEquivalences = Maps.newHashMap(result.strongEquivalences());

        if (target.getEpisodeNumber() != null && target.getSeriesNumber() != null) {
            stitch(target, combinedEquivalences, strongEquivalences, desc);
        } else {
            desc.appendText("No series/episode number");
        }
        desc.finishStage();

        result = new EquivalenceResult<Item>(target, rawScores, DefaultScoredEquivalents.fromMappedEquivs(combinedSource, combinedEquivalences), strongEquivalences, result.description());
        
        delegate.handle(result);
    }

    private void stitch(Episode target, Map<Item, Score> combinedEquivalences, Map<Publisher, ScoredEquivalent<Item>> strongEquivalences, ResultDescription desc) {
        for (List<Episode> childList : strongContainerChildren) {
            for (Episode ep : childList) {
                if(matchingSequenceNumbers(target, ep)) {
                    if (strongEquivalences.get(ep.getPublisher()) != null) {
                        desc.appendText("%s: existing strong equiv %s not overwritten by %s", ep.getPublisher(), strongEquivalences.get(ep.getPublisher()).equivalent(), ep);
                    } else {
                        if(!combinedEquivalences.containsKey(ep)) {
                            combinedEquivalences.put(ep, Score.valueOf(2.0));
                        }
                        strongEquivalences.put(ep.getPublisher(), ScoredEquivalent.<Item>equivalentScore(ep, Score.valueOf(2.0)));
                        desc.appendText("%s: found matching sequence item %s", ep.getPublisher(), ep);
                    }
                    break;
                }
            }
        }
    }

    public boolean matchingSequenceNumbers(Episode target, Episode ep) {
        return target.getEpisodeNumber().equals(ep.getEpisodeNumber()) && target.getSeriesNumber().equals(ep.getSeriesNumber());
    }
    
}
