package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;

public interface EquivalenceScorer<T> {

    ScoredCandidates<T> score(T subject, Iterable<T> suggestions, ResultDescription desc);
    
}
