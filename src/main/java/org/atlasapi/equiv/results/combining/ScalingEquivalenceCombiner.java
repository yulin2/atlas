package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScaledScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.content.Content;

public class ScalingEquivalenceCombiner<T extends Content> implements ScoreCombiner<T> {
    
    public static <T extends Content> ScalingEquivalenceCombiner<T> scale(ScoreCombiner<T> delegate, double scaler) {
        return new ScalingEquivalenceCombiner<T>(delegate, scaler);
    }

    private final ScoreCombiner<T> delegate;
    private final double scaler;

    public ScalingEquivalenceCombiner(ScoreCombiner<T> delegate, double scaler) {
        this.delegate = delegate;
        this.scaler = scaler;
    }
    
    @Override
    public ScoredCandidates<T> combine(List<ScoredCandidates<T>> scoredEquivalents, ResultDescription desc) {
        return ScaledScoredEquivalents.scale(delegate.combine(scoredEquivalents, desc), scaler);
    }

}
