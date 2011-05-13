package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

public class ThresholdEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {
    
    public static <T extends Content> ThresholdEquivalenceExtractor<T> fromPercent(int percent) {
        return new ThresholdEquivalenceExtractor<T>(percent/100.0);
    }

    private final Double threshold;

    public ThresholdEquivalenceExtractor(Double threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(List<ScoredEquivalent<T>> equivalents) {
        if(equivalents.isEmpty()) {
            return Maybe.nothing();
        }
        
        Double total = sum(equivalents);
        
        ScoredEquivalent<T> strongest = equivalents.get(0);
        if(strongest.score() / total > threshold) {
            return Maybe.just(strongest);
        }
        
        return Maybe.nothing();
    }

    private Double sum(List<ScoredEquivalent<T>> equivalents) {
        Double total = 0.0;
        
        for (ScoredEquivalent<T> scoredEquivalent : equivalents) {
            total += scoredEquivalent.score();
        }
        
        return total;
    }

}
