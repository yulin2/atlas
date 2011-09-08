package org.atlasapi.equiv.results;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Maps.filterValues;
import static com.google.common.collect.Maps.transformValues;
import static org.atlasapi.equiv.results.scores.ScoredEquivalent.equivalentScore;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
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
    
    private Function<Collection<ScoredEquivalent<T>>, ScoredEquivalent<T>> extractorFunction(final T target, final ResultDescription desc) {
        return new Function<Collection<ScoredEquivalent<T>>, ScoredEquivalent<T>>() {
            @Override
            public ScoredEquivalent<T> apply(Collection<ScoredEquivalent<T>> input) {
                Maybe<ScoredEquivalent<T>> extracted = extractor.extract(target, order(input), desc);
                return extracted.hasValue() ? extracted.requireValue() : null;
            }

            private List<ScoredEquivalent<T>> order(Collection<ScoredEquivalent<T>> input) {
                return Ordering.natural().reverse().immutableSortedCopy(input);
            }
        };
    }

    public EquivalenceResultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> extractor) {
        this.combiner = combiner;
        this.extractor = extractor;
    }

    public EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents) {
        ReadableDescription desc = new DefaultDescription();
        ScoredEquivalents<T> combined = combine(equivalents, desc);
        return new EquivalenceResult<T>(target, equivalents, combined, extract(target, combined, desc), desc);
    }

    private Map<Publisher, ScoredEquivalent<T>> extract(T target, ScoredEquivalents<T> combined, ResultDescription desc) {
        return filterValues(transformValues(publisherBin(combined.equivalents()), extractorFunction(target, desc)), notNull());
    }
    
    private Map<Publisher, Collection<ScoredEquivalent<T>>> publisherBin(Map<T, Score> equivalents) {
        Multimap<Publisher, ScoredEquivalent<T>> publisherBins = LinkedListMultimap.create();
        
        for (Entry<T, Score> equivalentScore : equivalents.entrySet()) {
            T key = equivalentScore.getKey();
            publisherBins.put(key.getPublisher(), equivalentScore(key, equivalentScore.getValue()));
        }
        
        return publisherBins.asMap();
    }

    private ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> equivalents, ResultDescription desc) {
        return !equivalents.isEmpty() ? combiner.combine(equivalents, desc) : DefaultScoredEquivalents.<T>fromSource("empty combination").build();
    }
}
