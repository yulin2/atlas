package org.atlasapi.equiv.handlers;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

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
        Map<Publisher, ScoredCandidate<Item>> strongEquivalences = filter(result.strongEquivalences(), desc);
        desc.finishStage();
        delegate.handle(new EquivalenceResult<Item>(result.target(), result.rawScores(), result.combinedEquivalences(), strongEquivalences, desc));

    }

    private Map<Publisher, ScoredCandidate<Item>> filter(Map<Publisher, ScoredCandidate<Item>> strongItems, final ResultDescription desc) {
        return ImmutableMap.copyOf(Maps.filterValues(strongItems, new Predicate<ScoredCandidate<Item>>() {
            @Override
            public boolean apply(ScoredCandidate<Item> input) {
                Collection<String> validContainers = containerRefs.get(input.candidate().getPublisher());
                if (validContainers == null || validContainers.isEmpty() || input.candidate().getContainer() == null || validContainers.contains(input.candidate().getContainer().getUri())) {
                    return true;
                }
                desc.appendText("%s removed. Unacceptable container: %s", input, input.candidate().getContainer().getUri());
                return false;
            }
        }));
    }

}
