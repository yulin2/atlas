package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;

public interface ScoreCombiner<T> {

    ScoredCandidates<T> combine(List<ScoredCandidates<T>> scoredEquivalents, ResultDescription desc);
    
}
