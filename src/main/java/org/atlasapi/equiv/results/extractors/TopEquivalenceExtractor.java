package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

public class TopEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    public static <T extends Content> TopEquivalenceExtractor<T> create() {
        return new TopEquivalenceExtractor<T>();
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(List<ScoredEquivalent<T>> equivalents) {
        if(equivalents == null | equivalents.size() < 1) {
            return Maybe.nothing();
        }
        return Maybe.just(equivalents.get(0));
        
    }

}
