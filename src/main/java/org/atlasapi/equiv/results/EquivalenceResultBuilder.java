package org.atlasapi.equiv.results;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
import org.atlasapi.equiv.results.marking.EquivalenceMarker;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class EquivalenceResultBuilder<T extends Content> {

    public static <T extends Content> EquivalenceResultBuilder<T> from(EquivalenceCombiner<T> combiner, EquivalenceMarker<T> marker) {
        return new EquivalenceResultBuilder<T>(combiner, marker);
    }

    private final EquivalenceCombiner<T> combiner;
    private final EquivalenceMarker<T> marker;

    public EquivalenceResultBuilder(EquivalenceCombiner<T> combiner, EquivalenceMarker<T> marker) {
        this.combiner = combiner;
        this.marker = marker;
    }

    public EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents) {
        return new EquivalenceResult<T>(target, equivalents, mark(combine(equivalents)));
    }

    private ScoredEquivalents<T> mark(ScoredEquivalents<T> combined) {
        Map<Publisher, List<ScoredEquivalent<T>>> ordered = Maps.transformValues(combined.getOrderedEquivalents(), new Function<List<ScoredEquivalent<T>>, List<ScoredEquivalent<T>>>() {
            @Override
            public List<ScoredEquivalent<T>> apply(List<ScoredEquivalent<T>> input) {
                return marker.mark(input);
            }
        });
        return ScoredEquivalents.fromOrderedEquivs(combined.source(), ordered);
    }

    private ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> equivalents) {
        return !equivalents.isEmpty() ? combiner.combine(equivalents) : ScoredEquivalents.<T> fromSource("empty combination").build();
    }

}
