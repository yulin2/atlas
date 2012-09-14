package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class MusicEquivalenceExtractor implements EquivalenceExtractor<Item> {

    private static final double SINGLE_THRESHOLD = 0.2;
    private static final double MULTI_THRESHOLD = 0.7;

    @Override
    public Maybe<ScoredCandidate<Item>> extract(Item target, List<ScoredCandidate<Item>> candidates, ResultDescription desc) {
        if (candidates.isEmpty()) {
            return Maybe.nothing();
        }
        
        desc.startStage(toString());
        
        List<ScoredCandidate<Item>> positiveScores = removeNonPositiveScores(candidates, desc);
        
        Maybe<ScoredCandidate<Item>> result = Maybe.nothing();
        
        if (positiveScores.size() == 1) {
            ScoredCandidate<Item> only = positiveScores.get(0);
            result = candidateIfOverThreshold(only, SINGLE_THRESHOLD, desc);
        } else if (positiveScores.size() > 1) {
            ScoredCandidate<Item> only = positiveScores.get(0);
            result = candidateIfOverThreshold(only, MULTI_THRESHOLD, desc);
        }
        
        desc.finishStage();
        return result; 
    }

    private List<ScoredCandidate<Item>> removeNonPositiveScores(List<ScoredCandidate<Item>> candidates, ResultDescription desc) {
        List<ScoredCandidate<Item>> positiveScores = Lists.newLinkedList();
        for (ScoredCandidate<Item> candidate : candidates) {
            if (candidate.score().asDouble() > 0.0) {
                positiveScores.add(candidate);
            } else {
                desc.appendText("%s removed (non-positive score)", candidate.candidate());
            }
        }
        return positiveScores;
    }

    private Maybe<ScoredCandidate<Item>> candidateIfOverThreshold(ScoredCandidate<Item> only, double threshold, ResultDescription desc) {
        if (only.score().asDouble() > threshold) {
            desc.appendText("%s beats %s threshold", only.candidate(), threshold);
            return Maybe.just(only);
        } else {
            desc.appendText("%s under %s threshold", only.candidate(), threshold);
            return Maybe.nothing();
        }
    }

    @Override
    public String toString() {
        return "Music Filter";
    }
    
}
