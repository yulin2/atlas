package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ItemEquivalenceUpdater<T extends Item> implements ContentEquivalenceUpdater<T> {

    private final EquivalenceGenerators<T> generators;
    private final EquivalenceScorers<T> scorers;
    private final EquivalenceResultBuilder<T> resultBuilder;
    private final ScoredEquivalentsMerger merger = new ScoredEquivalentsMerger();

    public ItemEquivalenceUpdater(EquivalenceGenerators<T> generators, EquivalenceScorers<T> scorers, EquivalenceResultBuilder<T> resultBuilder) {
        this.generators = generators;
        this.scorers = scorers;
        this.resultBuilder = resultBuilder;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        
        List<ScoredEquivalents<T>> generatedScores = generators.generate(content);
        
        List<T> generatedSuggestions = extractGeneratedSuggestions(generatedScores);
        
        List<ScoredEquivalents<T>> scoredScores = scorers.score(content, generatedSuggestions);
        
        return resultBuilder.resultFor(content, ImmutableList.copyOf(merger.merge(generatedScores, scoredScores)));
    }

    private List<T> extractGeneratedSuggestions(Iterable<ScoredEquivalents<T>> generatedScores) {
        return Lists.newArrayList(Iterables.concat(Iterables.transform(generatedScores, new Function<ScoredEquivalents<T>, Iterable<T>>() {
            @Override
            public Iterable<T> apply(ScoredEquivalents<T> input) {
                return input.equivalents().keySet();
            }
        })));
    }

}
