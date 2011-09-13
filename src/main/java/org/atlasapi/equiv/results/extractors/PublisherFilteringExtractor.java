package org.atlasapi.equiv.results.extractors;

import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class PublisherFilteringExtractor<T extends Content> extends FilteringEquivalenceExtractor<T> {

    @Override
    protected String name() {
        return "Publisher filter";
    }
    
    public PublisherFilteringExtractor(EquivalenceExtractor<T> delegate) {
        super(delegate, new EquivalenceFilter<T>() {
            
            Map<Publisher, Set<Publisher>> unacceptablePublishers = ImmutableMap.<Publisher, Set<Publisher>>of(
                    Publisher.BBC,  ImmutableSet.of(Publisher.C4, Publisher.ITV, Publisher.FIVE),
                    Publisher.C4,   ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.FIVE),
                    Publisher.ITV,  ImmutableSet.of(Publisher.BBC, Publisher.C4, Publisher.FIVE),
                    Publisher.FIVE, ImmutableSet.of(Publisher.BBC, Publisher.C4, Publisher.ITV));
            
            @Override
            public boolean apply(ScoredEquivalent<T> input, T subject, ResultDescription desc) {
                if (input.equivalent().getPublisher() == subject.getPublisher()) {
                    return false;
                }
                Set<Publisher> unacceptable = unacceptablePublishers.get(subject.getPublisher());
                if(unacceptable == null) {
                    return true;
                }
                
                boolean passes = !unacceptable.contains(input.equivalent().getPublisher());
                if (!passes) {
                    desc.appendText("%s removed. %s ∈ %s", input, input.equivalent().getPublisher(), unacceptable);
                }
                return passes;
            }
        });
    }
    
}
