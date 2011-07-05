package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class EquivalenceResult<T extends Content> {

    private final T target;
    private final List<ScoredEquivalents<T>> scores;
    private final ScoredEquivalents<T> combined;
    private final Map<Publisher, ScoredEquivalent<T>> strong;

    public EquivalenceResult(T target, List<ScoredEquivalents<T>> scores, ScoredEquivalents<T> combined, Map<Publisher, ScoredEquivalent<T>> strong) {
        this.target = target;
        this.scores = ImmutableList.copyOf(scores);
        this.combined = combined;
        this.strong = ImmutableMap.copyOf(strong);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", target(), scores);
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof EquivalenceResult) {
            EquivalenceResult<?> other = (EquivalenceResult<?>) that;
            return Objects.equal(target(), other.target()) && Objects.equal(scores, other.scores);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(target(), scores);
    }
    
    public Map<Publisher, Map<T, Score>> combinedEquivalences() {
        return this.combined.equivalents();
    }
    
    public Map<Publisher, ScoredEquivalent<T>> strongEquivalences() {
        return strong;
    }

    public T target() {
        return target;
    }

    public List<ScoredEquivalents<T>> rawScores() {
        return scores;
    }

    public EquivalenceResult<T> rebuildWith(EquivalenceResultBuilder<T> builder) {
        return builder.resultFor(target, scores);
    }
    
    
}
