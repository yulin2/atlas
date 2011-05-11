package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Set;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class BasicEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final Set<ContentEquivalenceGenerator<T>> calculators;
    private final EquivalenceResultBuilder<T> builder;

    public BasicEquivalenceUpdater(Set<ContentEquivalenceGenerator<T>> calculators, EquivalenceResultBuilder<T> builder) {
        this.calculators = calculators;
        this.builder = builder;
    }
    
    public EquivalenceResult<T> updateEquivalences(final T content) {
        List<ScoredEquivalents<T>> scores = ImmutableList.copyOf(Iterables.transform(calculators, new Function<ContentEquivalenceGenerator<T>, ScoredEquivalents<T>>() {
            @Override
            public ScoredEquivalents<T> apply(ContentEquivalenceGenerator<T> input) {
                return input.generateEquivalences(content);
            }
        }));
        return builder.resultFor(content, scores);
    }
    
}
