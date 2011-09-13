package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

/**
 * Always selects the equivalent with the highest score
 */
public class TopEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    public static <T extends Content> TopEquivalenceExtractor<T> create() {
        return new TopEquivalenceExtractor<T>();
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents, ResultDescription desc) {
        if(equivalents == null || equivalents.isEmpty()) {
            return Maybe.nothing();
        }
        return Maybe.just(equivalents.get(0));
        
    }

}
