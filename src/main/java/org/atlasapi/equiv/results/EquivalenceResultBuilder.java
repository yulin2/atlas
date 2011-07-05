package org.atlasapi.equiv.results;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Maps.filterValues;
import static com.google.common.collect.Maps.transformValues;
import static org.atlasapi.equiv.results.ScoredEquivalent.equivalentScore;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.base.Maybe;

public class EquivalenceResultBuilder<T extends Content> {

    public static <T extends Content> EquivalenceResultBuilder<T> resultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> marker) {
        return new EquivalenceResultBuilder<T>(combiner, marker);
    }

    private final EquivalenceCombiner<T> combiner;
    private final EquivalenceExtractor<T> extractor;
    
    private Function<Collection<ScoredEquivalent<T>>, ScoredEquivalent<T>> extractorFunction(final T target) {
        return new Function<Collection<ScoredEquivalent<T>>, ScoredEquivalent<T>>() {
            @Override
            public ScoredEquivalent<T> apply(Collection<ScoredEquivalent<T>> input) {
                Maybe<ScoredEquivalent<T>> extracted = extractor.extract(target, order(input));
                return extracted.hasValue() ? extracted.requireValue() : null;
            }

            private List<ScoredEquivalent<T>> order(Collection<ScoredEquivalent<T>> input) {
                return Ordering.natural().reverse().immutableSortedCopy(input);
            }
        };
    }

    public EquivalenceResultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> marker) {
        this.combiner = combiner;
        this.extractor = marker;
    }

    public EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents) {
        ScoredEquivalents<T> combined = combine(equivalents);
        return new EquivalenceResult<T>(target, equivalents, combined, extract(target, combined));
    }

    private Map<Publisher, ScoredEquivalent<T>> extract(T target, ScoredEquivalents<T> combined) {
        return filterValues(transformValues(publisherBin(combined.equivalents()), extractorFunction(target)), notNull());
    }
    
    private Map<Publisher, Collection<ScoredEquivalent<T>>> publisherBin(Map<T, Score> equivalents) {
        Multimap<Publisher, ScoredEquivalent<T>> publisherBins = LinkedListMultimap.create();
        
        for (Entry<T, Score> equivalentScore : equivalents.entrySet()) {
            T key = equivalentScore.getKey();
            publisherBins.put(key.getPublisher(), equivalentScore(key, equivalentScore.getValue()));
        }
        
        return publisherBins.asMap();
    }

    private ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> equivalents) {
        return !equivalents.isEmpty() ? combiner.combine(equivalents) : DefaultScoredEquivalents.<T>fromSource("empty combination").build();
    }
}
