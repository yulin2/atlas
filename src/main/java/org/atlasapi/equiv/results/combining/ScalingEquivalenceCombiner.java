package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

public class ScalingEquivalenceCombiner<T extends Content> implements EquivalenceCombiner<T> {

    private final EquivalenceCombiner<T> delegate;
    private final double scaler;

    public ScalingEquivalenceCombiner(EquivalenceCombiner<T> delegate, double scaler) {
        this.delegate = delegate;
        this.scaler = scaler;
    }
    
    @Override
    public ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents) {
        return  ScaledScoredEquivalents.scale(delegate.combine(scoredEquivalents), scaler);
    }

}
