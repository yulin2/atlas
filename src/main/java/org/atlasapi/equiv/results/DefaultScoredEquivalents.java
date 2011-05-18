package org.atlasapi.equiv.results;

import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class DefaultScoredEquivalents<T extends Content> implements ScoredEquivalents<T> {

    public static final <T extends Content> ScoredEquivalentsBuilder<T> fromSource(String source) {
        return new ScoredEquivalentsBuilder<T>(source);
    }

    public static final class ScoredEquivalentsBuilder<T extends Content> {

        private final String source;
        private final Map<Publisher, Map<T, Double>> equivs;

        public ScoredEquivalentsBuilder(String source) {
            this.source = source;
            this.equivs = Maps.newHashMap();
        }

        public ScoredEquivalentsBuilder<T> addEquivalent(T equivalent, double score) {
            Map<T, Double> current = equivs.get(equivalent.getPublisher());
            if (current == null) {
                current = Maps.newHashMap();
                equivs.put(equivalent.getPublisher(), current);
            }
            Double currentScore = current.get(equivalent);
            current.put(equivalent, score + (currentScore == null ? 0 : currentScore));
            return this;
        }

        public ScoredEquivalents<T> build() {
            return fromMappedEquivs(source, equivs);
        }
    }
    
    public static <T extends Content> ScoredEquivalents<T> fromMappedEquivs(String source, Map<Publisher, Map<T, Double>> equivs) {
        return new DefaultScoredEquivalents<T>(source, ImmutableMap.copyOf(equivs));
    }
    
    private final String source;
    private final Map<Publisher, Map<T, Double>> equivs;

    private DefaultScoredEquivalents(String source, Map<Publisher, Map<T, Double>> equivs) {
        this.source = source;
        this.equivs = equivs;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public Map<Publisher, Map<T, Double>> equivalents() {
        return equivs;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof DefaultScoredEquivalents) {
            DefaultScoredEquivalents<?> other = (DefaultScoredEquivalents<?>) that;
            return Objects.equal(source, other.source) && Objects.equal(equivs, other.equivs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source, equivs);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", source);
    }

}
