package org.atlasapi.equiv.results;

import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ScaledScoredEquivalents<T extends Content> implements ScoredEquivalents<T> {
    
    public static <T extends Content> ScaledScoredEquivalents<T> scale(ScoredEquivalents<T> input, double scaler) {
        if(input instanceof ScaledScoredEquivalents) {
            ScaledScoredEquivalents<T> scaledInput = (ScaledScoredEquivalents<T>) input;
            return new ScaledScoredEquivalents<T>(scaledInput.base, scaledInput.scaler);
        } else {
            return new ScaledScoredEquivalents<T>(input, scaler);
        }
    }

    private final ScoredEquivalents<T> base;
    private final double scaler;

    private ScaledScoredEquivalents(ScoredEquivalents<T> base, double scaler) {
        this.base = base;
        this.scaler = scaler;
    }
    
    @Override
    public String source() {
        return base.source();
    }

    @Override
    public Map<Publisher, Map<T, Double>> equivalents() {
        return scale(base.equivalents());
    }

    private Map<Publisher, Map<T, Double>> scale(Map<Publisher, Map<T, Double>> equivalents) {
        return ImmutableMap.copyOf(Maps.transformValues(equivalents, new Function<Map<T, Double>, Map<T, Double>>() {
            @Override
            public Map<T, Double> apply(Map<T, Double> input) {
                return ImmutableMap.copyOf(Maps.transformValues(input, new Function<Double, Double>() {
                    @Override
                    public Double apply(Double input) {
                        return input * scaler;
                    }
                }));
            }
        }));
    }

}
