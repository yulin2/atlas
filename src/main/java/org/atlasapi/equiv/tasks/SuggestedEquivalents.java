package org.atlasapi.equiv.tasks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.stats.Counter;

public class SuggestedEquivalents<T> {

    //Processes a multimap of suggestions into an publisher-binned map of ordered list of counts
    public static <T extends Described> SuggestedEquivalents<T> from(Multimap<Publisher, T> binnedSuggestions) {
        Map<Publisher, List<Count<T>>> binnedCountedSuggestions = Maps.transformValues(binnedSuggestions.asMap(), new Function<Collection<T>, List<Count<T>>>() {
            @Override
            public List<Count<T>> apply(Collection<T> input) {
                Counter<T, T> counter = new Counter<T, T>();
                for (T t : input) {
                    counter.incrementCount(t, t);
                }
                return Ordering.natural().reverse().immutableSortedCopy(counter.counts(Ordering.usingToString()));
            }
        });
        return new SuggestedEquivalents<T>(binnedCountedSuggestions);
    }

    private final Map<Publisher, List<Count<T>>> binnedCountedSuggestions;

    public SuggestedEquivalents(Map<Publisher, List<Count<T>>> binnedCountedSuggestions) {
        this.binnedCountedSuggestions = binnedCountedSuggestions;
    }

    public Map<Publisher, List<Count<T>>> getBinnedCountedSuggestions() {
        return binnedCountedSuggestions;
    }

    public Map<Publisher, T> strongSuggestions(double certainty) {
        return Maps.transformValues(binnedStrongSuggestions(certainty), Count.<T>unpackTarget());
    }

    private Map<Publisher, Count<T>> binnedStrongSuggestions(double certainty) {
        Map<Publisher, Count<T>> results = Maps.newHashMapWithExpectedSize(binnedCountedSuggestions.size());

        for (Entry<Publisher, List<Count<T>>> countBin : binnedCountedSuggestions.entrySet()) {
            Count<T> certainResult = strongResult(countBin.getValue(), certainty);
            if (certainResult != null) {
                results.put(countBin.getKey(), certainResult);
            }
        }

        return results;
    }

    private Count<T> strongResult(List<Count<T>> counts, double certainty) {
        Long total = 0L;
        for (Count<T> count : counts) {
            total += count.getCount();
        }
        return counts.get(0).getCount() / total.doubleValue() > certainty ? counts.get(0) : null;
    }
    
    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        }
        if(that instanceof SuggestedEquivalents) {
            SuggestedEquivalents<?> other = (SuggestedEquivalents<?>) that;
            return Objects.equal(binnedCountedSuggestions, other.binnedCountedSuggestions);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(binnedCountedSuggestions);
    }
    
    @Override
    public String toString() {
        return toString(null);
    }
    
    public String toString(Double certainty) {
        if(binnedCountedSuggestions.isEmpty()) {
            return "nothing";
        }
        
        final Set<Count<T>> strongSuggestions = certainty == null ? ImmutableSet.<Count<T>>of() : allStrongSuggestions(certainty);
        return Joiner.on(", ").join(Iterables.transform(allSuggestions(), new Function<Count<T>, String>() {
            @Override
            public String apply(Count<T> input) {
                T target = input.getTarget();
                return String.format("%s(%s):%s%s", target, input.getCount(), strongSuggestions.contains(input) ? " STRONG" : "");
            }
        }));
    }
    
    private List<Count<T>> allSuggestions() {
        return Ordering.natural().reverse().immutableSortedCopy(Iterables.concat(binnedCountedSuggestions.values()));
    }

    private Set<Count<T>> allStrongSuggestions(double certainty) {
        return ImmutableSet.copyOf(Ordering.natural().immutableSortedCopy(binnedStrongSuggestions(certainty).values()));
    }

    public SuggestedEquivalents<String> stringResult(Function<? super T, String> transformer) {
        final Function<Count<T>, Count<String>> targetTransformer = Count.targetTransformer(Ordering.natural(), transformer);
        return new SuggestedEquivalents<String>(Maps.transformValues(binnedCountedSuggestions, new Function<List<Count<T>>, List<Count<String>>>() {
            @Override
            public List<Count<String>> apply(List<Count<T>> input) {
                return ImmutableList.copyOf(Iterables.transform(input, targetTransformer));
            }
        }));
    }
}
