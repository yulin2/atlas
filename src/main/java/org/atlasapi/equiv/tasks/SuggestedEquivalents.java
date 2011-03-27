package org.atlasapi.equiv.tasks;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.notNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.metabroadcast.common.stats.Count;

public class SuggestedEquivalents<T> {

    // Processes a multimap of suggestions into an publisher-binned map of ordered list of counts
    public static <T extends Described> SuggestedEquivalents<T> from(Multimap<Publisher, T> binnedSuggestions) {
        Map<Publisher, List<Count<T>>> binnedCountedSuggestions = Maps.transformValues(binnedSuggestions.asMap(), new Function<Collection<T>, List<Count<T>>>() {
            @Override
            public List<Count<T>> apply(Collection<T> input) {
                List<Count<T>> counts = Lists.newArrayList();
                Set<T> seen = Sets.newHashSet();
                for (T t : Iterables.filter(Iterables.filter(input, notNull()), not(in(seen)))) {
                    counts.add(new Count<T>(t, Ordering.usingToString(), Collections.frequency(input, t)));
                    seen.add(t);
                }
                return counts.isEmpty()? null : Ordering.natural().reverse().immutableSortedCopy(counts);
            }
        });
        return new SuggestedEquivalents<T>(Maps.filterValues(binnedCountedSuggestions, Predicates.notNull()));
    }
    
    public static <T extends Described> SuggestedEquivalents<T> merge(SuggestedEquivalents<T> first, SuggestedEquivalents<T> second) {
        Preconditions.checkArgument(Sets.intersection(first.binnedCountedSuggestions.keySet(), second.binnedCountedSuggestions.keySet()).isEmpty(), "Cannot merge suggested equivalents");
        
        ImmutableMap.Builder<Publisher, List<Count<T>>> suggestions = ImmutableMap.builder();
        
        suggestions.putAll(first.binnedCountedSuggestions);
        suggestions.putAll(second.binnedCountedSuggestions);
        
        return new SuggestedEquivalents<T>(suggestions.build());
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
        if(that instanceof SuggestedEquivalents<?>) {
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
        
        final List<Count<T>> strongSuggestions = certainty == null ? ImmutableList.<Count<T>>of() : allStrongSuggestions(certainty);
        return Joiner.on(", ").join(Iterables.transform(allSuggestions(), new Function<Count<T>, String>() {
            @Override
            public String apply(Count<T> input) {
                T target = input.getTarget();
                return String.format("%s(%s):%s%s", target, input.getCount(), strongSuggestions.contains(input) ? " STRONG" : "");
            }
        }));
    }
    
    public List<Count<T>> allSuggestions() {
        return Ordering.natural().reverse().immutableSortedCopy(Iterables.concat(binnedCountedSuggestions.values()));
    }

    public List<Count<T>> allStrongSuggestions(double certainty) {
        return Ordering.natural().immutableSortedCopy(binnedStrongSuggestions(certainty).values());
    }

    public <V extends Comparable<? super V>> SuggestedEquivalents<V> transform(Function<? super T, V> transformer) {
        final Function<Count<T>, Count<V>> targetTransformer = Count.targetTransformer(Ordering.natural(), transformer);
        return new SuggestedEquivalents<V>(Maps.transformValues(binnedCountedSuggestions, new Function<List<Count<T>>, List<Count<V>>>() {
            @Override
            public List<Count<V>> apply(List<Count<T>> input) {
                return ImmutableList.copyOf(Iterables.transform(input, targetTransformer));
            }
        }));
    }
}
