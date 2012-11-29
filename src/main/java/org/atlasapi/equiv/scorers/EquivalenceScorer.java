package org.atlasapi.equiv.scorers;

import java.util.Set;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;

public interface EquivalenceScorer<T> {

    ScoredCandidates<T> score(T subject, Set<? extends T> candidates, ResultDescription desc);
    
}
