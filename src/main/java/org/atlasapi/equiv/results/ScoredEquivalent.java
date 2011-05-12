package org.atlasapi.equiv.results;

import org.atlasapi.media.entity.Content;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.primitives.Doubles;


public final class ScoredEquivalent<T extends Content> implements Comparable<ScoredEquivalent<?>> {

    private final T target;
    private final double score;
    private final boolean strong;
    
    public static final <T extends Content> ScoredEquivalent<T> equivalentScore(T equivalent, double score) {
        return new ScoredEquivalent<T>(equivalent, score, false);
    }
    
    public static final <T extends Content> Predicate<ScoredEquivalent<T>> strongFilter() {
        return new Predicate<ScoredEquivalent<T>>() {
            @Override
            public boolean apply(ScoredEquivalent<T> input) {
                return input.isStrong();
            }
        };
    }

    private ScoredEquivalent(T equivalent, double score, boolean strong) {
        target = equivalent;
        this.score = score;
        this.strong = strong;
    }

    public T equivalent() {
        return target;
    }

    public double score() {
        return score;
    }

    public boolean isStrong() {
        return strong;
    }
    
    public ScoredEquivalent<T> copyAsStrong() {
        return new ScoredEquivalent<T>(target, score, true);
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof ScoredEquivalent) {
            ScoredEquivalent<?> other = (ScoredEquivalent<?>) that;
            return target.equals(other.target) && score == other.score;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(target, score);
    }
    
    @Override
    public String toString() {
        return String.format("%s : %+.2f", strong ? target.getCanonicalUri().toUpperCase() : target.getCanonicalUri(), score);
    }

    @Override
    public int compareTo(ScoredEquivalent<?> o) {
        return Doubles.compare(score, o.score);
    }
}
