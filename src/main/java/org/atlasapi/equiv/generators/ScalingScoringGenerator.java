package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.equiv.scorers.ScalingEquivalenceScorer;
import org.atlasapi.media.content.Content;

import com.google.common.base.Function;

public class ScalingScoringGenerator<T extends Content> implements ContentEquivalenceGenerator<T>, ContentEquivalenceScorer<T> {
    
    public static <T extends Content, SG extends ContentEquivalenceGenerator<T>&ContentEquivalenceScorer<T>> ScalingScoringGenerator<T> from(SG scoringGenerator, Function<Double, Double> scaler) {
        return new ScalingScoringGenerator<T>(ScalingEquivalenceGenerator.scale(scoringGenerator, scaler), ScalingEquivalenceScorer.scale(scoringGenerator, scaler));
    }

    private final ScalingEquivalenceGenerator<T> generator;
    private final ScalingEquivalenceScorer<T> scorer;
    
    private ScalingScoringGenerator(ScalingEquivalenceGenerator<T> generator, ScalingEquivalenceScorer<T> scorer) {
        this.generator = generator;
        this.scorer = scorer;
    }


    @Override
    public ScoredEquivalents<T> score(T content, Iterable<T> suggestions, ResultDescription desc) {
        return scorer.score(content, suggestions, desc);
    }

    @Override
    public ScoredEquivalents<T> generate(T content, ResultDescription desc) {
        return generator.generate(content, desc);
    }
    
    @Override
    public String toString() {
        //scorer/generator are same instance, scaled same.
        return generator.toString();
    }
}
