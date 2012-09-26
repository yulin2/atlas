package org.atlasapi.equiv.results.scores;

import java.util.Map;

import com.google.common.base.Function;

public interface ScoredCandidates<T> {

    String source();

    Map<T, Score> candidates();
    
    public static final Function<ScoredCandidates<?>, String> TO_SOURCE = new Function<ScoredCandidates<?>, String>() {
        @Override
        public String apply(ScoredCandidates<?> input) {
            return input.source();
        }
    };
 
}