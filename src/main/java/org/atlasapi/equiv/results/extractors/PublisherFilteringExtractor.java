package org.atlasapi.equiv.results.extractors;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class PublisherFilteringExtractor<T extends Content> implements EquivalenceExtractor<T> {

    private final EquivalenceExtractor<T> innerExtractor;
    
    private final Map<Publisher, Set<Publisher>> unacceptablePublishers = ImmutableMap.<Publisher, Set<Publisher>>of(
            Publisher.BBC,  ImmutableSet.of(Publisher.C4, Publisher.ITV, Publisher.FIVE),
            Publisher.C4,   ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.FIVE),
            Publisher.ITV,  ImmutableSet.of(Publisher.BBC, Publisher.C4, Publisher.FIVE),
            Publisher.FIVE, ImmutableSet.of(Publisher.BBC, Publisher.C4, Publisher.ITV)
    );
    
    public PublisherFilteringExtractor(EquivalenceExtractor<T> delegate) {
        innerExtractor = new FilteringEquivalenceExtractor<T>(delegate, new EquivalenceFilter<T>() {
            @Override
            public boolean apply(ScoredEquivalent<T> input, T subject) {
                if(input.equivalent().getPublisher() == subject.getPublisher()) {
                    return false;
                }
                Set<Publisher> unacceptable = unacceptablePublishers.get(input.equivalent().getPublisher());
                return unacceptable == null || !unacceptable.contains(subject.getPublisher());
            }
        });
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents) {
        return innerExtractor.extract(target, equivalents);
    }

}
