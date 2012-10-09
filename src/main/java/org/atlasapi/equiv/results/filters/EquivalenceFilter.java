package org.atlasapi.equiv.results.filters;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

public interface EquivalenceFilter<T> {
    
    Iterable<ScoredCandidate<T>> apply(Iterable<ScoredCandidate<T>> candidate, T subject, ResultDescription desc);
    
}
