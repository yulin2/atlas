package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BasicEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final Set<ContentEquivalenceGenerator<T>> calculators;
    private final EquivalenceResultBuilder<T> builder;

    public BasicEquivalenceUpdater(Set<ContentEquivalenceGenerator<T>> calculators, EquivalenceResultBuilder<T> builder) {
        this.calculators = calculators;
        this.builder = builder;
    }
    
    public EquivalenceResult<T> updateEquivalences(final T content) {
        
        Set<T> suggestions = Sets.newHashSet();
        List<ScoredEquivalents<T>> scores = Lists.newArrayList();
        
        for (ContentEquivalenceGenerator<T> calculator : calculators) {
            ScoredEquivalents<T> scoredEquivalents = calculator.generateEquivalences(content, suggestions);
            suggestions.addAll(extractSuggestions(scoredEquivalents.equivalents()));
            scores.add(scoredEquivalents);
        }
        
        return builder.resultFor(content, scores);
    }

    private List<T> extractSuggestions(Map<Publisher, Map<T, Double>> equivalents) {
        return Lists.newArrayList(Iterables.concat(Iterables.transform(equivalents.values(), new Function<Map<T, Double>, Iterable<T>>() {
            @Override
            public Iterable<T> apply(Map<T, Double> input) {
                return input.keySet();
            }
        })));
    }
    
}
