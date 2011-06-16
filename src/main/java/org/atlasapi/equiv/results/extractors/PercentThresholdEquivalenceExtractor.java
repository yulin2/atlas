package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

/**
 * Selects the equivalent with the highest score given its score is above a given percentage threshold of the total of all equivalents' scores
 */
public class PercentThresholdEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {
    
    public static <T extends Content> PercentThresholdEquivalenceExtractor<T> fromPercent(int percent) {
        return new PercentThresholdEquivalenceExtractor<T>(percent/100.0);
    }

    private final Double threshold;

    public PercentThresholdEquivalenceExtractor(Double threshold) {
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
            double score = scoredEquivalent.score();
            if(score > 0) {
                total += score;
            }
        }
        
        return total;
    }

}
