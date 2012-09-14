package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

import com.metabroadcast.common.base.Maybe;

/**
 * Selects the equivalent with the highest score given its score is above a given percentage threshold of the total of all equivalents' scores
 */
public class PercentThresholdEquivalenceExtractor<T> implements EquivalenceExtractor<T> {
    
    public static <T> PercentThresholdEquivalenceExtractor<T> moreThanPercent(int percent) {
        return new PercentThresholdEquivalenceExtractor<T>(percent/100.0);
    }

    private final Double threshold;

    public PercentThresholdEquivalenceExtractor(Double threshold) {
        this.threshold = threshold;
    }
    
    private static final String NAME = "Percent Extractor";
    
    @Override
    public Maybe<ScoredCandidate<T>> extract(T target, List<ScoredCandidate<T>> equivalents, ResultDescription desc) {
        desc.startStage(NAME);
        
        if (equivalents.isEmpty()) {
            desc.appendText("no equivalents").finishStage();
            return Maybe.nothing();
        }
        
        Double total = sum(equivalents);

        ScoredCandidate<T> strongest = equivalents.get(0);
        if (strongest.score().isRealScore() && strongest.score().asDouble() / total > threshold) {
            desc.appendText("%s extracted. %s / %s > %s", strongest.candidate(), strongest.score(), total, threshold).finishStage();
            return Maybe.just(strongest);
        }
        
        desc.appendText("%s not extracted. %s / %s < %s", strongest.candidate(), strongest.score(), total, threshold).finishStage();
        return Maybe.nothing();
    }

    private Double sum(List<ScoredCandidate<T>> equivalents) {
        Double total = 0.0;
        
        for (ScoredCandidate<T> scoredEquivalent : equivalents) {
            Score score = scoredEquivalent.score();
            if(score.isRealScore() && score.asDouble() > 0) {
                total += score.asDouble();
            }
        }
        
        return total;
    }

}
