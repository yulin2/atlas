package org.atlasapi.equiv.results.filters;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

import com.google.common.collect.ImmutableList;

public class ConjunctiveFilter<T> extends AbstractEquivalenceFilter<T> {
    
    public static final <T> EquivalenceFilter<T> valueOf(Iterable<? extends EquivalenceFilter<T>> filters) {
        return new ConjunctiveFilter<T>(filters);
    }
    
    private final List<EquivalenceFilter<T>> filters;

    public ConjunctiveFilter(Iterable<? extends EquivalenceFilter<T>> filters) {
        this.filters = ImmutableList.copyOf(filters);
    }
    
    @Override
    boolean doFilter(ScoredCandidate<T> candidate, T subject, ResultDescription desc) {
        boolean result = true;
        for (EquivalenceFilter<T> filter : filters) {
            result &= filter.apply(candidate, subject, desc);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "all of: " + filters;
    }

}
