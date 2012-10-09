package org.atlasapi.equiv.results.filters;

import javax.annotation.Nullable;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class AbstractEquivalenceFilter<T> implements EquivalenceFilter<T> {

    @Override
    public final Iterable<ScoredCandidate<T>> apply(Iterable<ScoredCandidate<T>> input, final T subject, final ResultDescription desc) {
        desc.startStage(toString());
        Iterable<ScoredCandidate<T>> result = Iterables.filter(input, new Predicate<ScoredCandidate<T>>() {
            @Override
            public boolean apply(@Nullable ScoredCandidate<T> input) {
                return doFilter(input, subject, desc);
            }
        });
        desc.finishStage();
        return result;
    }

    protected abstract boolean doFilter(ScoredCandidate<T> input, T subject, ResultDescription desc);
    
}
