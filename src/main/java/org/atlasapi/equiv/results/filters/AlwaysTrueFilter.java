package org.atlasapi.equiv.results.filters;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

public class AlwaysTrueFilter<T> extends AbstractEquivalenceFilter<T> {

    @Override
    protected boolean doFilter(ScoredCandidate<T> input, T subject, ResultDescription desc) {
        return true;
    }

}
