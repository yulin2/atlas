package org.atlasapi.equiv.handlers;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.atlasapi.equiv.ContentRef;
import org.atlasapi.equiv.EquivalenceSummary;
import org.atlasapi.equiv.EquivalenceSummaryStore;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.util.Identifiables;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EquivalenceSummaryWritingHandler<T extends Content> implements EquivalenceResultHandler<T> {

    private final EquivalenceSummaryStore equivSummaryStore;

    public EquivalenceSummaryWritingHandler(EquivalenceSummaryStore equivSummaryStore) {
        this.equivSummaryStore = equivSummaryStore;
    }

    @Override
    public void handle(EquivalenceResult<T> result) {
        equivSummaryStore.store(summaryOf(result));
    }

    private EquivalenceSummary summaryOf(EquivalenceResult<T> result) {
        Id id = result.subject().getId();
        Id parent = parentOf(result.subject());
        List<Id> candidates = candidatesFrom(result.combinedEquivalences());
        Map<Publisher, ContentRef> equivalents = equivalentsFrom(result.strongEquivalences());
        return new EquivalenceSummary(id,parent,candidates,equivalents);
    }

    private Id parentOf(T subject) {
        if (subject instanceof Item) {
            ParentRef container = ((Item)subject).getContainer();
            if (container != null) {
                return container.getId();
            }
        } else if (subject instanceof Series) {
            ParentRef container = ((Series)subject).getParent();
            if (container != null) {
                return container.getId();
            }
        }
        return null;
    }

    private List<Id> candidatesFrom(ScoredCandidates<T> combinedEquivalences) {
        return ImmutableList.copyOf(Iterables.transform(combinedEquivalences.candidates().keySet(), Identifiables.toId()));
    }

    private Map<Publisher, ContentRef> equivalentsFrom(Map<Publisher, ScoredCandidate<T>> strongEquivalences) {
        return ImmutableMap.copyOf(Maps.transformValues(strongEquivalences, new Function<ScoredCandidate<T>, ContentRef>() {
            @Override
            public ContentRef apply(@Nullable ScoredCandidate<T> input) {
                return contentRefFrom(input.candidate());
            }
        }));
    }
    
    private ContentRef contentRefFrom(T candidate) {
        Id id = candidate.getId();
        Publisher publisher = candidate.getPublisher();
        Id parent = parentOf(candidate);
        return new ContentRef(id, publisher, parent);
    }

}
