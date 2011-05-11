package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.extractor.EquivalenceExtractor;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class EquivalenceResult<T extends Content> {

    private final T target;
    private final List<ScoredEquivalents<T>> scores;
    private final Map<Publisher, List<ScoredEquivalent<T>>> strongEquivalences;

    public EquivalenceResult(T target, List<ScoredEquivalents<T>> scores, EquivalenceExtractor<T> extractor) {
        this.target = target;
        this.scores = ImmutableList.copyOf(scores);
        this.strongEquivalences = extractEquivalences(extractor);
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

    private Map<Publisher, List<ScoredEquivalent<T>>> extractEquivalences(final EquivalenceExtractor<T> extractor) {
        return Maps.transformValues(combine(scores()), new Function<List<ScoredEquivalent<T>>, List<ScoredEquivalent<T>>>() {
            @Override
            public List<ScoredEquivalent<T>> apply(List<ScoredEquivalent<T>> input) {
                return extractor.extractFrom(input);
            }
        });
    }

    private Map<Publisher, List<ScoredEquivalent<T>>> combine(List<ScoredEquivalents<T>> scores) {
        ScoredEquivalents<T> combined = null;
        for (ScoredEquivalents<T> scoredEquivalents : scores) {
            combined = scoredEquivalents.combine(combined);
        }
        return combined != null ? combined.getOrderedEquivalents() : ImmutableMap.<Publisher, List<ScoredEquivalent<T>>>of();
    }
    
    public EquivalenceResult<T> copyWithExtractor(EquivalenceExtractor<T> extractor) {
        return new EquivalenceResult<T>(target(), scores(), extractor);
    }
    
    public Map<Publisher, List<ScoredEquivalent<T>>> strongEquivalences() {
        return this.strongEquivalences;
    }

    public T target() {
        return target;
    }

    public List<ScoredEquivalents<T>> scores() {
        return scores;
    }
    
    
}
