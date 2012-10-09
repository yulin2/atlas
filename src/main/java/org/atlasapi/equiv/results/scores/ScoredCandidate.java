package org.atlasapi.equiv.results.scores;

import java.util.Comparator;

import org.atlasapi.media.entity.Content;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Ordering;

public final class ScoredCandidate<T> {
    
    public static final Ordering<ScoredCandidate<?>> SCORE_ORDERING = Ordering.from(new Comparator<ScoredCandidate<?>>() {
        @Override
        public int compare(ScoredCandidate<?> o1, ScoredCandidate<?> o2) {
            return Score.SCORE_ORDERING.compare(o1.score(), o2.score());
        }
    });

    public static final <T> ScoredCandidate<T> valueOf(T equivalent, Score score) {
        return new ScoredCandidate<T>(equivalent, score);
    }
    
    public static final <T extends Content> Function<ScoredCandidate<T>, T> toCandidate() {
        return new Function<ScoredCandidate<T>, T>() {
            @Override
            public T apply(ScoredCandidate<T> input) {
                return input.candidate();
            }
        };
    }
    
    private final T candidate;
    private final Score score;

    private ScoredCandidate(T candidate, Score score) {
        this.candidate = candidate;
        this.score = score;
    }

    public T candidate() {
        return candidate;
    }

    public Score score() {
        return score;
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof ScoredCandidate) {
            ScoredCandidate<?> other = (ScoredCandidate<?>) that;
            return candidate.equals(other.candidate) && score.equals(other.score);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(candidate, score);
    }
    
    @Override
    public String toString() {
        return String.format("%s : %s", candidate, score);
    }

}
