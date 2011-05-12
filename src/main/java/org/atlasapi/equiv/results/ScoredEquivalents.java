package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
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
            return fromMappedEquivs(source, ImmutableMap.copyOf(equivs));
        }
    }
    
    public static <T extends Content> ScoredEquivalents<T> fromMappedEquivs(String source, Map<Publisher, Map<T, Double>> equivs) {
        return new ScoredEquivalents<T>(source, ImmutableMap.copyOf(equivs), computeOrderedEquivs(equivs));
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

    public Map<Publisher, Map<T, Double>> getMappedEquivalents() {
        return equivs;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof ScoredEquivalents) {
            ScoredEquivalents<?> other = (ScoredEquivalents<?>) that;
            return Objects.equal(source, other.source) && Objects.equal(equivs, other.equivs) && Objects.equal(ordered, other.ordered);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source, equivs, ordered);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", source, ordered);
    }

//    public ScoredEquivalents<T> combine(EquivalenceCombiner<T> combiner, ScoredEquivalents<T> other) {
//        if(other == null) {
//            return this;
//        }
//        Map<Publisher, Map<T, Double>> combined = Maps.newHashMap();
//        for (Publisher publisher : ImmutableSet.copyOf(Iterables.concat(equivs.keySet(), other.equivs.keySet()))) {
//            combined.put(publisher, combiner.combine(equivs.get(publisher), other.equivs.get(publisher)));
//        }
//        return new ScoredEquivalents<T>(String.format("%s/%s", source, other.source), combined, computeOrderedEquivs(combined));
//    }

}
