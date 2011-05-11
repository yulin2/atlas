package org.atlasapi.equiv.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class ScoredEquivalents<T extends Content> {

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
            return new ScoredEquivalents<T>(source, equivs);
        }
    }

    private final String source;
    private final Map<Publisher, Map<T, Double>> equivs;

    private ScoredEquivalents(String source, Map<Publisher, Map<T, Double>> equivs) {
        this.source = source;
        this.equivs = ImmutableMap.copyOf(equivs);
    }

    public String source() {
        return source;
    }

    public Map<Publisher, List<ScoredEquivalent<T>>> getOrderedEquivalents() {
        return Maps.transformValues(equivs, new Function<Map<T, Double>, List<ScoredEquivalent<T>>>() {

            @Override
            public List<ScoredEquivalent<T>> apply(Map<T, Double> input) {
                List<ScoredEquivalent<T>> scores = Lists.newArrayListWithCapacity(input.size());
                for (Entry<T, Double> equivScore : input.entrySet()) {
                    scores.add(ScoredEquivalent.equivalentScore(equivScore.getKey(), equivScore.getValue()));
                }
                return Ordering.natural().reverse().immutableSortedCopy(scores);
            }
        });
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof ScoredEquivalents) {
            ScoredEquivalents<?> other = (ScoredEquivalents<?>) that;
            return Objects.equal(source, other.source) && Objects.equal(equivs, other.equivs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", source, equivs);
    }

    public ScoredEquivalents<T> combine(ScoredEquivalents<T> other) {
        if(other == null) {
            return this;
        }
        Map<Publisher, Map<T, Double>> combined = Maps.newHashMap();
        for (Publisher publisher : ImmutableSet.copyOf(Iterables.concat(equivs.keySet(), other.equivs.keySet()))) {
            combined.put(publisher, combine(equivs.get(publisher), other.equivs.get(publisher)));
        }
        return new ScoredEquivalents<T>(String.format("%s/%s", source, other.source), combined);
    }

    private Map<T, Double> combine(Map<T, Double> left, Map<T, Double> right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        HashMap<T, Double> combined = Maps.newHashMap();
        for (T equiv : ImmutableSet.copyOf(Iterables.concat(left.keySet(), right.keySet()))) {
            combined.put(equiv, add(left.get(equiv),right.get(equiv)));
        }
        return combined;
    }

    private Double add(Double left, Double right) {
        return left != null ? (right != null ? left + right : left) : right;
    }
}
