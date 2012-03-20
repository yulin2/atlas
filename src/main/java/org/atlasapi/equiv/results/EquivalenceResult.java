package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class EquivalenceResult<T extends Content> {
    
    public static final <T extends Content> Function<EquivalenceResult<T>, T> toTarget() {
        return new Function<EquivalenceResult<T>, T>() {

            @Override
            public T apply(EquivalenceResult<T> input) {
                return input.target();
            }
        };
    }

    private final T target;
    private final List<ScoredEquivalents<T>> scores;
    private final ScoredEquivalents<T> combined;
    private final Map<Publisher, ScoredEquivalent<T>> strong;
    private final ReadableDescription desc;

    public EquivalenceResult(T target, List<ScoredEquivalents<T>> scores, ScoredEquivalents<T> combined, Map<Publisher, ScoredEquivalent<T>> strong, ReadableDescription desc) {
        this.target = target;
        this.scores = ImmutableList.copyOf(scores);
        this.combined = combined;
        this.strong = ImmutableMap.copyOf(strong);
        this.desc = desc;
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
    
    public ScoredEquivalents<T> combinedEquivalences() {
        return this.combined;
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

    public ReadableDescription description() {
        return desc;
    }

    public EquivalenceResult<T> rebuildWith(EquivalenceResultBuilder<T> builder) {
        return builder.resultFor(target, scores, desc);
    }
    
    
}
