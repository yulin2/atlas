package org.atlasapi.equiv.tasks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.stats.Counter;

public class EquivalentSuggestions<T> {

    public static <T extends Described> EquivalentSuggestions<T> from(Multimap<Publisher, T> binnedSuggestions) {
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
        return new EquivalentSuggestions<T>(binnedCountedSuggestions);
    }

    private final Map<Publisher, List<Count<T>>> binnedCountedSuggestions;

    public EquivalentSuggestions(Map<Publisher, List<Count<T>>> binnedCountedSuggestions) {
        this.binnedCountedSuggestions = binnedCountedSuggestions;
    }

    public List<Count<T>> allSuggestions() {
        return Ordering.natural().reverse().immutableSortedCopy(Iterables.concat(binnedCountedSuggestions.values()));
    }

    public Set<Count<T>> allMatchedSuggestions(double certainty) {
        return ImmutableSet.copyOf(Ordering.natural().immutableSortedCopy(binnedMatchedSuggestions(certainty).values()));
    }

    public Map<Publisher, T> matchedSuggestions(double certainty) {
        return Maps.transformValues(binnedMatchedSuggestions(certainty), Count.<T>unpackTarget());
    }

    private Map<Publisher, Count<T>> binnedMatchedSuggestions(double certainty) {
        Map<Publisher, Count<T>> results = Maps.newHashMapWithExpectedSize(binnedCountedSuggestions.size());

        for (Entry<Publisher, List<Count<T>>> countBin : binnedCountedSuggestions.entrySet()) {
            Count<T> certainResult = certainResult(countBin.getValue(), certainty);
            if (certainResult != null) {
                results.put(countBin.getKey(), certainResult);
            }
        }

        return results;
    }

    private Count<T> certainResult(List<Count<T>> counts, double certainty) {
        Long total = 0L;
        for (Count<T> count : counts) {
            total += count.getCount();
        }
        return counts.get(0).getCount() / total.doubleValue() > certainty ? counts.get(0) : null;
    }

    public Map<Publisher, List<Count<T>>> getBinnedCountedSuggestions() {
        return binnedCountedSuggestions;
    }
}
