package org.atlasapi.equiv.generators;

import java.util.List;

import org.atlasapi.equiv.EquivalenceSummary;
import org.atlasapi.equiv.EquivalenceSummaryStore;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates.Builder;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.util.Identifiables;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.collect.OptionalMap;

public class CandidateContainerEquivalenceGenerator implements EquivalenceGenerator<Item> {

    private final ContentResolver contentResolver;
    private final EquivalenceSummaryStore equivSummaryStore;
    
    private final Function<Container, Iterable<Item>> TO_CHILD_REFS = new Function<Container, Iterable<Item>>() {
        @Override
        public Iterable<Item> apply(@Nullable Container input) {
            Iterable<Id> childIds = Iterables.transform(input.getChildRefs(), Identifiables.toId());
            ResolvedContent children = contentResolver.findByIds(childIds);
            return Iterables.filter(children.getAllResolvedResults(), Item.class);
        }
    };

    public CandidateContainerEquivalenceGenerator(ContentResolver contentResolver, EquivalenceSummaryStore equivSummaryStore) {
        this.contentResolver = contentResolver;
        this.equivSummaryStore = equivSummaryStore;
    }

    @Override
    public ScoredCandidates<Item> generate(Item subject, ResultDescription desc) {
        Builder<Item> result = DefaultScoredCandidates.fromSource("Container");

        ParentRef parent = subject.getContainer();
        if (parent != null) {
            Id parentId = parent.getId();
            OptionalMap<Id, EquivalenceSummary> containerSummary = parentSummary(parentId);
            Optional<EquivalenceSummary> optional = containerSummary.get(parentId);
            if (optional.isPresent()) {
                EquivalenceSummary summary = optional.get();
                for (Item child : childrenOf(summary.getCandidates(), result)) {
                    result.addEquivalent(child, Score.NULL_SCORE);
                }
            }
        }

        return result.build();
    }

    private Iterable<Item> childrenOf(ImmutableList<Id> candidates, Builder<Item> result) {
        List<Identified> resolvedContent = contentResolver.findByIds(candidates).getAllResolvedResults();
        Iterable<Container> resolvedContainers = Iterables.filter(resolvedContent, Container.class);
        return Iterables.concat(Iterables.transform(resolvedContainers, TO_CHILD_REFS));
    }

    private OptionalMap<Id, EquivalenceSummary> parentSummary(Id parentId) {
        return equivSummaryStore.summariesForIds(ImmutableSet.of(parentId));
    }

}
