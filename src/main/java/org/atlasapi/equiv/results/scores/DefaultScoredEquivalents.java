package org.atlasapi.equiv.results.scores;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class DefaultScoredEquivalents<T> implements ScoredCandidates<T> {

    public static final <T> ScoredEquivalentsBuilder<T> fromSource(String source) {
        return new ScoredEquivalentsBuilder<T>(source);
    }

    public static final class ScoredEquivalentsBuilder<T> {

        private final String source;
        private final Map<T, Score> equivs;

        public ScoredEquivalentsBuilder(String source) {
            this.source = source;
            this.equivs = Maps.newHashMap();
        }

        public ScoredEquivalentsBuilder<T> addEquivalent(T equivalent, Score score) {
            Score current = equivs.get(equivalent);
            equivs.put(equivalent, current == null ? score : score.add(current));
            return this;
        }

        public ScoredCandidates<T> build() {
            return fromMappedEquivs(source, equivs);
        }
    }
    
    public static <T> ScoredCandidates<T> fromMappedEquivs(String source, Map<T, Score> equivs) {
        return new DefaultScoredEquivalents<T>(source, ImmutableMap.copyOf(equivs));
    }
    
    private final String source;
    private final Map<T, Score> candidates;

    private DefaultScoredEquivalents(String source, Map<T, Score> equivs) {
        this.source = source;
        this.candidates = equivs;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public Map<T, Score> candidates() {
        return candidates;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof DefaultScoredEquivalents) {
            DefaultScoredEquivalents<?> other = (DefaultScoredEquivalents<?>) that;
            return Objects.equal(source, other.source) && Objects.equal(candidates, other.candidates);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source, candidates);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", source, candidates);
    }

}
