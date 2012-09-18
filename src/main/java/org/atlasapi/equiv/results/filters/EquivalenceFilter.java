package org.atlasapi.equiv.results.filters;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

public interface EquivalenceFilter<T> {
    
    boolean apply(ScoredCandidate<T> candidate, T subject, ResultDescription desc);
    
}
