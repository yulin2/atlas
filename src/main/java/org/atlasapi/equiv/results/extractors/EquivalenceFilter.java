package org.atlasapi.equiv.results.extractors;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

public interface EquivalenceFilter<T> {
    
    boolean apply(ScoredCandidate<T> input, T subject, ResultDescription desc);
    
}
