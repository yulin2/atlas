package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.extractor.EquivalenceCombiner;
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
            return new ScoredEquivalents<T>(source, ImmutableMap.copyOf(equivs), computeOrderedEquivs(equivs));
        }
    }
    
    private static <T extends Content> Map<Publisher, List<ScoredEquivalent<T>>> computeOrderedEquivs(Map<Publisher,Map<T,Double>> equivs) {
        return ImmutableMap.copyOf(Maps.transformValues(equivs, new Function<Map<T, Double>, List<ScoredEquivalent<T>>>() {

            @Override
            public List<ScoredEquivalent<T>> apply(Map<T, Double> input) {
                List<ScoredEquivalent<T>> scores = Lists.newArrayListWithCapacity(input.size());
                for (Entry<T, Double> equivScore : input.entrySet()) {
                    scores.add(ScoredEquivalent.equivalentScore(equivScore.getKey(), equivScore.getValue()));
                }
                return Ordering.natural().reverse().immutableSortedCopy(scores);
            }
        }));
    }
    
    public static <T extends Content> ScoredEquivalents<T> fromOrderedEquivs(String source, Map<Publisher, List<ScoredEquivalent<T>>> ordered) {
        return new ScoredEquivalents<T>(source, computeEquivs(ordered), ordered);
    }
    
    private static <T extends Content> Map<Publisher, Map<T, Double>> computeEquivs(Map<Publisher, List<ScoredEquivalent<T>>> equivs) {
        return ImmutableMap.copyOf(Maps.transformValues(equivs, new Function<List<ScoredEquivalent<T>>, Map<T, Double>>() {
            @Override
            public Map<T, Double> apply(List<ScoredEquivalent<T>> input) {
                return ImmutableMap.copyOf(Maps.transformValues(Maps.uniqueIndex(input, new Function<ScoredEquivalent<T>, T>() {
                    @Override
                    public T apply(ScoredEquivalent<T> input) {
                        return input.equivalent();
                    }
                }), new Function<ScoredEquivalent<T>, Double>() {
                    @Override
                    public Double apply(ScoredEquivalent<T> input) {
                        return input.score();
                    }
                }));
            }
        }));
    }

    private final String source;
    private final Map<Publisher, Map<T, Double>> equivs;
    private final Map<Publisher, List<ScoredEquivalent<T>>> ordered;

    private ScoredEquivalents(String source, Map<Publisher, Map<T, Double>> equivs, Map<Publisher, List<ScoredEquivalent<T>>> ordered) {
        this.source = source;
        this.equivs = equivs;
        this.ordered= ordered;
    }

    public String source() {
        return source;
    }

    public Map<Publisher, List<ScoredEquivalent<T>>> getOrderedEquivalents() {
        return this.ordered;
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

    public ScoredEquivalents<T> combine(EquivalenceCombiner<T> combiner, ScoredEquivalents<T> other) {
        if(other == null) {
            return this;
        }
        Map<Publisher, Map<T, Double>> combined = Maps.newHashMap();
        for (Publisher publisher : ImmutableSet.copyOf(Iterables.concat(equivs.keySet(), other.equivs.keySet()))) {
            combined.put(publisher, combiner.combine(equivs.get(publisher), other.equivs.get(publisher)));
        }
        return new ScoredEquivalents<T>(String.format("%s/%s", source, other.source), combined, computeOrderedEquivs(combined));
    }

}
