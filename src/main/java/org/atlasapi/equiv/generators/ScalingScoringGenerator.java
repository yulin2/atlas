package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.equiv.scorers.ScalingEquivalenceScorer;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Function;

public class ScalingScoringGenerator<T extends Content> implements ContentEquivalenceGenerator<T>, ContentEquivalenceScorer<T> {
    
    public static <T extends Content, SG extends ContentEquivalenceGenerator<T>&ContentEquivalenceScorer<T>> ScalingScoringGenerator<T> from(SG scoringGenerator, Function<Double, Double> scaler) {
        return new ScalingScoringGenerator<T>(ScalingEquivalenceGenerator.scale(scoringGenerator, scaler), ScalingEquivalenceScorer.scale(scoringGenerator, scaler));
    }

    private final ScalingEquivalenceGenerator<T> generator;
    private final ScalingEquivalenceScorer<T> scorer;
    
    public ScalingScoringGenerator(ScalingEquivalenceGenerator<T> generator, ScalingEquivalenceScorer<T> scorer) {
        this.generator = generator;
        this.scorer = scorer;
    }


    @Override
    public ScoredEquivalents<T> score(T content, Iterable<T> suggestions) {
        return scorer.score(content, suggestions);
    }

    @Override
    public ScoredEquivalents<T> generate(T content) {
        return generator.generate(content);
    }
    
}
