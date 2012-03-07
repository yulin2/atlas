package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.ContentEquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ItemEquivalenceUpdater<T extends Item> implements ContentEquivalenceUpdater<T> {

    private final EquivalenceGenerators<T> generators;
    private final EquivalenceScorers<T> scorers;
    private final EquivalenceResultBuilder<T> resultBuilder;
    private final ScoredEquivalentsMerger merger = new ScoredEquivalentsMerger();

    public ItemEquivalenceUpdater(Iterable<ContentEquivalenceGenerator<T>> generators, Iterable<ContentEquivalenceScorer<T>> scorers, EquivalenceResultBuilder<T> resultBuilder, AdapterLog log) {
        this.generators = EquivalenceGenerators.from(generators,log);
        this.scorers = EquivalenceScorers.from(scorers,log);
        this.resultBuilder = resultBuilder;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content, Optional<List<T>> externalCandidates) {
        
        ReadableDescription desc = new DefaultDescription();
        
        List<ScoredEquivalents<T>> generatedScores = generators.generate(content, desc);
        
        List<T> suggestions = ImmutableList.<T>builder()
                .addAll(extractGeneratedSuggestions(generatedScores))
                .addAll(externalCandidates.or(ImmutableList.<T>of()))
                .build();
        
        List<ScoredEquivalents<T>> scoredScores = scorers.score(content, suggestions, desc);
        
        return resultBuilder.resultFor(content, ImmutableList.copyOf(merger.merge(generatedScores, scoredScores)), desc);
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
