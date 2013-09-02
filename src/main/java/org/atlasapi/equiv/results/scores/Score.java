package org.atlasapi.equiv.results.scores;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

public abstract class Score {
    
    public abstract double asDouble();
    
    public abstract boolean isRealScore();
    
    public abstract Score transform(Function<Double,Double> transformer);
    
    public abstract Score add(Score other);
    
    public static final Score NULL_SCORE = new NullScore();
    
    public static final Score nullScore() {
        return NULL_SCORE;
    }
    
    public static final Score ONE = Score.valueOf(1.0);
    public static final Score ZERO = Score.valueOf(0.0);
    
    private static final Score NEGATIVE_ONE = Score.valueOf(-1.0);
    
    public static final Score negativeOne() {
        return NEGATIVE_ONE;
    }
    
    public static final Predicate<Score> IS_REAL_SCORE = new Predicate<Score>() {

        @Override
        public boolean apply(Score input) {
            return input.isRealScore();
        }
        
    };

    private static class NullScore extends Score {

        @Override
        public double asDouble() {
            return Double.NaN;
        }

        @Override
        public boolean isRealScore() {
            return false;
        }

        @Override
        public Score transform(Function<Double, Double> transformer) {
            return this;
        }

        @Override
        public Score add(Score other) {
            return other;
        }
        
        @Override
        public String toString() {
            return "none";
        }
        
    }
    
    public static final Score valueOf(Double score) {
        if(score == null || score.isNaN()) {
            return NULL_SCORE;
        }
        return new RealScore(score);
    }
    
    private static class RealScore extends Score {

        private final double score;

        public RealScore(double rawScore) {
            this.score = rawScore;
        }
        
        @Override
        public double asDouble() {
            return score;
        }

        @Override
        public boolean isRealScore() {
            return true;
        }

        @Override
        public Score transform(Function<Double, Double> transformer) {
            return valueOf(transformer.apply(score));
        }

        @Override
        public Score add(Score other) {
            if(!other.isRealScore()) {
                return this;
            }
            return valueOf(score + other.asDouble());
        }
        
        @Override
        public String toString() {
            return String.format(" %+.2f", score);
        }
        
        @Override
        public boolean equals(Object that) {
            if(this == that) {
                return true;
            }
            if(that instanceof RealScore) {
                return score == ((RealScore)that).score;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return Doubles.hashCode(score);
        }
    }
    
    public static final Ordering<Score> SCORE_ORDERING = Ordering.from(new Comparator<Score>() {

        @Override
        public int compare(Score o1, Score o2) {
            if(o1.isRealScore() && o2.isRealScore()) {
                return Doubles.compare(o1.asDouble(), o2.asDouble());
            }
            if(o1.isRealScore()) {
                return 1;
            }
            if(o2.isRealScore()) {
                return -1;
            }
            return 0;
        }
        
    });
    
    public static final Function<Score, Score> transformerFrom(final Function<Double,Double> rawTransformer) {
        return new Function<Score, Score>() {

            @Override
            public Score apply(Score input) {
                return input.transform(rawTransformer);
            }
        };
    }
    
}
