package org.atlasapi.equiv.handlers;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Identified;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class EpisodeFilteringEquivalenceResultHandler implements EquivalenceResultHandler<Item> {

    private final EquivalenceResultHandler<Item> delegate;
    private final Multimap<Publisher,String> containerRefs;
    
    private static final Function<Container,Publisher> toPublisher = new Function<Container,Publisher>() {
        @Override
        public Publisher apply(Container input) {
            return input.getPublisher();
        }
    };

    public EpisodeFilteringEquivalenceResultHandler(EquivalenceResultHandler<Item> delegate, Set<Container> strongContainers) {
        this.delegate = delegate;
        this.containerRefs = Multimaps.transformValues(Multimaps.index(strongContainers, toPublisher), Identified.TO_URI);
    }

    @Override
    public void handle(EquivalenceResult<Item> result) {

        ReadableDescription desc = (ReadableDescription) result.description().startStage(String.format("Episode parent filter: %s", containerRefs));
        Map<Publisher, ScoredEquivalent<Item>> strongEquivalences = filter(result.strongEquivalences(), desc);
        desc.finishStage();
        delegate.handle(new EquivalenceResult<Item>(result.target(), result.rawScores(), result.combinedEquivalences(), strongEquivalences, desc));

    }

    private Map<Publisher, ScoredEquivalent<Item>> filter(Map<Publisher, ScoredEquivalent<Item>> strongItems, final ResultDescription desc) {
        return ImmutableMap.copyOf(Maps.filterValues(strongItems, new Predicate<ScoredEquivalent<Item>>() {
            @Override
            public boolean apply(ScoredEquivalent<Item> input) {
                Collection<String> validContainers = containerRefs.get(input.equivalent().getPublisher());
                if (validContainers == null || validContainers.isEmpty() || input.equivalent().getContainer() == null || validContainers.contains(input.equivalent().getContainer().getUri())) {
                    return true;
                }
                desc.appendText("%s removed. Unacceptable container: %s", input, input.equivalent().getContainer().getUri());
                return false;
            }
        }));
    }

}
