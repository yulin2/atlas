package org.atlasapi.equiv.results.scores;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.atlasapi.media.content.Content;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScaledScoredEquivalents<T extends Content> implements ScoredCandidates<T> {
    
    public static <T extends Content> ScaledScoredEquivalents<T> scale(ScoredCandidates<T> input, final double scaler) {
        return scale(input, new Function<Double, Double>() {
            @Override
            public Double apply(Double input) {
                return input * scaler;
            }
        });
    }

    public static <T extends Content> ScaledScoredEquivalents<T> scale(ScoredCandidates<T> input, Function<Double, Double> basicScale) {
        if(input instanceof ScaledScoredEquivalents) {
            ScaledScoredEquivalents<T> scaledInput = (ScaledScoredEquivalents<T>) input;
            return new ScaledScoredEquivalents<T>(scaledInput.base, Functions.compose(basicScale, scaledInput.scaler));
        } else {
            return new ScaledScoredEquivalents<T>(input, basicScale);
        }
    }
    
    private final ScoredCandidates<T> base;
    private final Function<Double, Double> scaler;

    private ScaledScoredEquivalents(ScoredCandidates<T> base, Function<Double, Double> scaler) {
        this.base = base;
        this.scaler = scaler;
    }
    
    @Override
    public String source() {
        return base.source();
    }

    @Override
    public Map<T, Score> candidates() {
        return scale(base.candidates());
    }

    private Map<T, Score> scale(Map<T, Score> equivalents) {
        return ImmutableMap.copyOf(Maps.transformValues(equivalents, Score.transformerFrom(scaler)));
    }

    @Override
    public String toString() {
        return String.format("Scaled %s", base);
    }

    @Override
    public List<ScoredCandidate<T>> orderedCandidates(Comparator<? super T> tieBreak) {
        return Lists.transform(base.orderedCandidates(tieBreak), new Function<ScoredCandidate<T>, ScoredCandidate<T>>() {
            @Override
            public ScoredCandidate<T> apply(@Nullable ScoredCandidate<T> input) {
                return ScoredCandidate.valueOf(input.candidate(), input.score().transform(scaler));
            }
        });
    }
}
