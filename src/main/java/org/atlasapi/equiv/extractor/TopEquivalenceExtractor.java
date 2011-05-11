package org.atlasapi.equiv.extractor;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.google.common.collect.ImmutableList;

public class TopEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    @Override
    public List<ScoredEquivalent<T>> extractFrom(List<ScoredEquivalent<T>> equivalents) {
        return equivalents == null | equivalents.size() < 1 ? ImmutableList.<ScoredEquivalent<T>>of() : ImmutableList.of(equivalents.get(0));
    }

}
