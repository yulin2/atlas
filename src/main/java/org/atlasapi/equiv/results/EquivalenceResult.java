package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class EquivalenceResult<T extends Content> {

    private final T target;
    private final List<ScoredEquivalents<T>> scores;
    private final ScoredEquivalents<T> combined;

    public EquivalenceResult(T target, List<ScoredEquivalents<T>> scores, ScoredEquivalents<T> combined) {
        this.target = target;
        this.scores = ImmutableList.copyOf(scores);
        this.combined = combined;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", target(), scores());
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof EquivalenceResult) {
            EquivalenceResult<?> other = (EquivalenceResult<?>) that;
            return Objects.equal(target(), other.target()) && Objects.equal(scores(), other.scores());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(target(), scores());
    }
    
    public Map<Publisher, List<ScoredEquivalent<T>>> combinedEquivalences() {
        return this.combined.getOrderedEquivalents();
    }
    
    public Map<Publisher, List<ScoredEquivalent<T>>> strongEquivalences() {
        return ImmutableMap.copyOf(Maps.transformValues(this.combined.getOrderedEquivalents(), new Function<List<ScoredEquivalent<T>>, List<ScoredEquivalent<T>>>() {
            @Override
            public List<ScoredEquivalent<T>> apply(List<ScoredEquivalent<T>> input) {
                return ImmutableList.copyOf(Iterables.filter(input, ScoredEquivalent.<T> strongFilter()));
            }
        }));
    }

    public T target() {
        return target;
    }

    public List<ScoredEquivalents<T>> scores() {
        return scores;
    }

    public EquivalenceResult<T> rebuildWith(EquivalenceResultBuilder<T> builder) {
        return builder.resultFor(target, scores);
    }
    
    
}
