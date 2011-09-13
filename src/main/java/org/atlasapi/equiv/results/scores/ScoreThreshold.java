package org.atlasapi.equiv.results.scores;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class ScoreThreshold implements Predicate<Score> {

    public static final ScoreThreshold greaterThan(double threshold) {
        return new GreaterThanThreshold(threshold);
    }
    
    public static final ScoreThreshold greaterThanOrEqual(double threshold) {
        return new GreaterThanEqualThreshold(threshold);
    }
    
    public static final ScoreThreshold not(ScoreThreshold threshold) {
        return new InverseThreshold(threshold);
    }
    
    public static final ScoreThreshold POSITIVE = greaterThan(0);
    public static final ScoreThreshold NON_NEGATIVE = greaterThanOrEqual(0);
    public static final ScoreThreshold NEGATIVE = not(greaterThanOrEqual(0));
    public static final ScoreThreshold NON_POSITIVE = not(greaterThan(0));
    
    private static final class InverseThreshold extends ScoreThreshold {

        private final Predicate<Score> delegate;

        public InverseThreshold(ScoreThreshold delegate) {
            this.delegate = Predicates.not(delegate);
        }
        
        @Override
        public boolean apply(Score input) {
            return delegate.apply(input);
        }
        
    }
    
    private static final class GreaterThanThreshold extends ScoreThreshold {
        private final double threshold;

        public GreaterThanThreshold(double threshold) {
            this.threshold = threshold;
        }
        
        @Override
        public boolean apply(Score input) {
            return input != null && input.isRealScore() && input.asDouble() > threshold;
        }
        
        @Override
        public String toString() {
            return String.format("greater than %s", threshold);
        }
    }
    
    private static final class GreaterThanEqualThreshold extends ScoreThreshold {
        private final double threshold;

        public GreaterThanEqualThreshold(double threshold) {
            this.threshold = threshold;
        }
        
        @Override
        public boolean apply(Score input) {
            return input != null && input.isRealScore() && input.asDouble() >= threshold;
        }

        @Override
        public String toString() {
            return String.format("greater than or equal %s", threshold);
        }
    }
}
