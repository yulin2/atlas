package org.atlasapi.equiv.extractor;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class TopEquivalenceMarker<T extends Content> implements EquivalenceMarker<T> {

    public static <T extends Content> TopEquivalenceMarker<T> create() {
        return new TopEquivalenceMarker<T>();
    }
    
    @Override
    public List<ScoredEquivalent<T>> mark(List<ScoredEquivalent<T>> equivalents) {
        if(equivalents == null | equivalents.size() < 1) {
            ImmutableList.<ScoredEquivalent<T>>of();
        }
        return ImmutableList.<ScoredEquivalent<T>>builder().add(equivalents.get(0).copyAsStrong()).addAll(Iterables.skip(equivalents, 1)).build();
        
    }

}
