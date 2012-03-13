package org.atlasapi.equiv.handlers;

import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EpisodeFilteringEquivalenceResultHandler implements EquivalenceResultHandler<Item> {

    private final EquivalenceResultHandler<Item> delegate;
    private final ImmutableSet<String> containerRefs;

    public EpisodeFilteringEquivalenceResultHandler(EquivalenceResultHandler<Item> delegate, Set<Container> strongContainers) {
        this.delegate = delegate;
        this.containerRefs = ImmutableSet.copyOf(Iterables.transform(strongContainers, Identified.TO_URI));
    }

    @Override
    public void handle(EquivalenceResult<Item> result) {

        ReadableDescription desc = (ReadableDescription) result.description().startStage(String.format("Episode parent filter: %s", containerRefs));
        Map<Publisher, ScoredEquivalent<Item>> strongEquivalences = filter(result.strongEquivalences(), desc);
        desc.finishStage();
        System.out.println();
        delegate.handle(new EquivalenceResult<Item>(result.target(), result.rawScores(), result.combinedEquivalences(), strongEquivalences, desc));

    }

    private Map<Publisher, ScoredEquivalent<Item>> filter(Map<Publisher, ScoredEquivalent<Item>> strongItems, final ResultDescription desc) {
        return ImmutableMap.copyOf(Maps.filterValues(strongItems, new Predicate<ScoredEquivalent<Item>>() {
            @Override
            public boolean apply(ScoredEquivalent<Item> input) {
                if (input.equivalent().getContainer() != null && !containerRefs.contains(input.equivalent().getContainer().getUri())) {
                    desc.appendText("%s removed. Unacceptable container: %s", input, input.equivalent().getContainer().getUri());
                    return false;
                }
                return true;
            }
        }));
    }

}
