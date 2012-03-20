package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Content;

public class ScalingEquivalenceCombiner<T extends Content> implements EquivalenceCombiner<T> {
    
    public static <T extends Content> ScalingEquivalenceCombiner<T> scale(EquivalenceCombiner<T> delegate, double scaler) {
        return new ScalingEquivalenceCombiner<T>(delegate, scaler);
    }

    private final EquivalenceCombiner<T> delegate;
    private final double scaler;

    public ScalingEquivalenceCombiner(EquivalenceCombiner<T> delegate, double scaler) {
        this.delegate = delegate;
        this.scaler = scaler;
    }
    
    @Override
    public ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents, ResultDescription desc) {
        return ScaledScoredEquivalents.scale(delegate.combine(scoredEquivalents, desc), scaler);
    }

}
