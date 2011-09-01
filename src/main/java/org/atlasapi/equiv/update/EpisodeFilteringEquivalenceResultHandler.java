package org.atlasapi.equiv.update;

import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EpisodeFilteringEquivalenceResultHandler implements EquivalenceResultHandler<Item> {

    private final EquivalenceResultHandler<Item> delegate;
    private final Set<Container> strongContainers;

    public EpisodeFilteringEquivalenceResultHandler(EquivalenceResultHandler<Item> delegate, Set<Container> strongContainers) {
        this.delegate = delegate;
        this.strongContainers = strongContainers;
    }

    @Override
    public void handle(EquivalenceResult<Item> result) {

        Map<Publisher, ScoredEquivalent<Item>> strongEquivalences = Maps.newHashMap(filter(result.strongEquivalences()));

        delegate.handle(new EquivalenceResult<Item>(result.target(), result.rawScores(), result.combinedEquivalences(), strongEquivalences));

    }

    private Map<Publisher, ScoredEquivalent<Item>> filter(Map<Publisher, ScoredEquivalent<Item>> strongItems) {

        final ImmutableSet<String> containerRefs = ImmutableSet.copyOf(Iterables.transform(strongContainers, Identified.TO_URI));

        return Maps.filterValues(Maps.transformValues(strongItems, new Function<ScoredEquivalent<Item>, ScoredEquivalent<Item>>() {
            @Override
            public ScoredEquivalent<Item> apply(ScoredEquivalent<Item> input) {
                if (!containerRefs.contains(input.equivalent().getContainer().getUri())) {
                    return null;
                }
                return input;
            }
        }), Predicates.notNull());
    }

}
