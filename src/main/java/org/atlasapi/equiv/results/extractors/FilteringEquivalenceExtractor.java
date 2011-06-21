package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class FilteringEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    public static <T extends Content> FilteringEquivalenceExtractor<T> filteringExtractor(EquivalenceExtractor<T> delegate, Predicate<ScoredEquivalent<T>> filter) {
        return new FilteringEquivalenceExtractor<T>(delegate, filter);
    }
    
    private final EquivalenceExtractor<T> delegate;
    private final Predicate<ScoredEquivalent<T>> filter;

    public FilteringEquivalenceExtractor(EquivalenceExtractor<T> delegate, Predicate<ScoredEquivalent<T>> filter) {
        this.delegate = delegate;
        this.filter = filter;
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(List<ScoredEquivalent<T>> equivalents) {
        return delegate.extract(ImmutableList.copyOf(Iterables.filter(equivalents, filter)));
    }

}
