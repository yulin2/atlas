package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.content.Content;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class FilteringEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {
    
    public static <T extends Content> FilteringEquivalenceExtractor<T> filteringExtractor(EquivalenceExtractor<T> delegate, EquivalenceFilter<T> filter) {
        return new FilteringEquivalenceExtractor<T>(delegate, filter);
    }
    
    private final EquivalenceExtractor<T> delegate;
    private final EquivalenceFilter<T> filter;

    public FilteringEquivalenceExtractor(EquivalenceExtractor<T> delegate, EquivalenceFilter<T> filter) {
        this.delegate = delegate;
        this.filter = filter;
    }
    
    protected String name() {
        return "Filtering Extractor";
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents, ResultDescription desc) {
        desc.startStage(name());
        ImmutableList<ScoredEquivalent<T>> filtered = ImmutableList.copyOf(Iterables.filter(equivalents, filter(target, filter, desc)));
        desc.finishStage();
        return delegate.extract(target, filtered, desc);
    }

    private Predicate<ScoredEquivalent<T>> filter(final T target, final EquivalenceFilter<T> filter, final ResultDescription desc) {
        return new Predicate<ScoredEquivalent<T>>() {

            @Override
            public boolean apply(ScoredEquivalent<T> input) {
                return filter.apply(input, target, desc);
            }
            
        };
    }

}
