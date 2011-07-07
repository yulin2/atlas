package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

public class MinimumScoreEquivalenceExtractor<T extends Content> extends ChainingEquivalenceExtractor<T> {
    
    public static <T extends Content> MinimumScoreEquivalenceExtractor<T> minimumFrom(EquivalenceExtractor<T> link, double minimum) {
        return new MinimumScoreEquivalenceExtractor<T>(link, minimum);
    }

    private final double minimum;

    public MinimumScoreEquivalenceExtractor(EquivalenceExtractor<T> link, double minimum) {
        super(link);
        this.minimum = minimum;
    }

    @Override
    protected Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents, Maybe<ScoredEquivalent<T>> delegateExtraction) {
        if(delegateExtraction.hasValue()) {
            ScoredEquivalent<T> realExtraction = delegateExtraction.requireValue();
            if(realExtraction.score().isRealScore() && realExtraction.score().asDouble() > minimum) {
                return Maybe.just(realExtraction);
            }
        }
        return Maybe.nothing();
    }

}
