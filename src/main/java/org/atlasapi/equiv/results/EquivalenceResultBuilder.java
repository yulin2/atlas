package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public class EquivalenceResultBuilder<T extends Content> {

    public static <T extends Content> EquivalenceResultBuilder<T> resultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> marker) {
        return new EquivalenceResultBuilder<T>(combiner, marker);
    }

    private final EquivalenceCombiner<T> combiner;
    private final EquivalenceExtractor<T> extractor;

    public EquivalenceResultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> marker) {
        this.combiner = combiner;
        this.extractor = marker;
    }

    public EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents) {
        ScoredEquivalents<T> combined = combine(equivalents);
        return new EquivalenceResult<T>(target, equivalents, combined, extract(combined));
    }

    private Map<Publisher, ScoredEquivalent<T>> extract(ScoredEquivalents<T> combined) {
        Map<Publisher, ScoredEquivalent<T>> ordered = Maps.filterValues(Maps.transformValues(combined.getOrderedEquivalents(), new Function<List<ScoredEquivalent<T>>, ScoredEquivalent<T>>() {
            @Override
            public ScoredEquivalent<T> apply(List<ScoredEquivalent<T>> input) {
                Maybe<ScoredEquivalent<T>> extracted = extractor.extract(input);
                return extracted.hasValue() ? extracted.requireValue() : null;
            }
        }), Predicates.notNull());
        return ordered;
    }

    private ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> equivalents) {
        return !equivalents.isEmpty() ? combiner.combine(equivalents) : ScoredEquivalents.<T> fromSource("empty combination").build();
    }

}
