package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.results.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Function;

public class ScalingEquivalenceScorer<T extends Content> implements ContentEquivalenceScorer<T> {

    private final ContentEquivalenceScorer<T> delegate;
    private final Function<Double, Double> scalingFunction;


    public static <T extends Content> ScalingEquivalenceScorer<T> scale(ContentEquivalenceScorer<T> delegate, final double scaler) {
        return scale(delegate, new Function<Double, Double>() {
            @Override
            public Double apply(Double input) {
                return input * scaler;
            }
        });
    }
    
    public static <T extends Content> ScalingEquivalenceScorer<T> scale(ContentEquivalenceScorer<T> delegate, Function<Double, Double> scalingFunction) {
      return new ScalingEquivalenceScorer<T>(delegate, scalingFunction);
    }
    
    public ScalingEquivalenceScorer(ContentEquivalenceScorer<T> delegate, Function<Double, Double> scalingFunction) {
        this.delegate = delegate;
        this.scalingFunction = scalingFunction;
    }

    
    @Override
    public ScoredEquivalents<T> score(T content, Iterable<T> suggestions) {
        return ScaledScoredEquivalents.<T>scale(delegate.score(content, suggestions), scalingFunction);
    }
    
}