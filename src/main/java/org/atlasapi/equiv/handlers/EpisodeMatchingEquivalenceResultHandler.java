package org.atlasapi.equiv.handlers;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
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
        if (!(result.subject() instanceof Episode)) {
            delegate.handle(result);
            return;
        }
        
        Episode target = (Episode) result.subject();
        
        ResultDescription desc = result.description().startStage("Episode sequence stitching");
        
        List<ScoredCandidates<Item>> rawScores = result.rawScores();

        String combinedSource = result.combinedEquivalences().source();
        Map<Item, Score> combinedEquivalences = Maps.newHashMap(result.combinedEquivalences().candidates());
        Map<Publisher, ScoredCandidate<Item>> strongEquivalences = Maps.newHashMap(result.strongEquivalences());

        if (target.getEpisodeNumber() != null && target.getSeriesNumber() != null) {
            stitch(target, combinedEquivalences, strongEquivalences, desc);
        } else {
            desc.appendText("No series/episode number");
        }
        desc.finishStage();

        result = new EquivalenceResult<Item>(target, rawScores, DefaultScoredCandidates.fromMappedEquivs(combinedSource, combinedEquivalences), strongEquivalences, result.description());
        
        delegate.handle(result);
    }

    private void stitch(Episode target, Map<Item, Score> combinedEquivalences, Map<Publisher, ScoredCandidate<Item>> strongEquivalences, ResultDescription desc) {
        for (List<Episode> childList : strongContainerChildren) {
            for (Episode ep : childList) {
                if(matchingSequenceNumbers(target, ep)) {
                    if (strongEquivalences.get(ep.getPublisher()) != null) {
                        desc.appendText("%s: existing strong equiv %s not overwritten by %s", ep.getPublisher(), strongEquivalences.get(ep.getPublisher()).candidate(), ep);
                    } else {
                        if(!combinedEquivalences.containsKey(ep)) {
                            combinedEquivalences.put(ep, Score.valueOf(2.0));
                        }
                        strongEquivalences.put(ep.getPublisher(), ScoredCandidate.<Item>valueOf(ep, Score.valueOf(2.0)));
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
