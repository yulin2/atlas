package org.atlasapi.equiv.results.filters;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

import com.google.common.collect.ImmutableList;

public class ConjunctiveFilter<T> implements EquivalenceFilter<T> {

    private final List<EquivalenceFilter<T>> filters;

    public ConjunctiveFilter(Iterable<? extends EquivalenceFilter<T>> filters) {
        this.filters = ImmutableList.copyOf(filters);
    }

    @Override
    public Iterable<ScoredCandidate<T>> apply(Iterable<ScoredCandidate<T>> candidate, T subject, ResultDescription desc) {
        desc.startStage(toString());
        Iterable<ScoredCandidate<T>> result = candidate;
        for (EquivalenceFilter<T> filter : filters) {
            result = filter.apply(result, subject, desc);
        }
        desc.finishStage();
        return result ;
    }

    @Override
    public String toString() {
        return "all of: " + filters;
    }
}
