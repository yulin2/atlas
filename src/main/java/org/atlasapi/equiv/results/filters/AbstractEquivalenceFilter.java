package org.atlasapi.equiv.results.filters;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

public abstract class AbstractEquivalenceFilter<T> implements EquivalenceFilter<T> {

    @Override
    public final boolean apply(ScoredCandidate<T> input, T subject, ResultDescription desc) {
        desc.startStage(toString());
        boolean result = doFilter(input, subject, desc);
        desc.finishStage();
        return result;
    }

    abstract boolean doFilter(ScoredCandidate<T> input, T subject, ResultDescription desc);
    
}
