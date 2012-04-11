package org.atlasapi.equiv.update;

import static com.google.common.base.Preconditions.checkNotNull;

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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ItemEquivalenceUpdater<T extends Item> implements ContentEquivalenceUpdater<T> {

    public static <T extends Item> Builder<T> builder(EquivalenceResultBuilder<T> resultBuilder, AdapterLog log) {
        return new Builder<T>(resultBuilder, log);
    }
    
    public static class Builder<T extends Item> {
        
        private final EquivalenceResultBuilder<T> resultBuilder;
        private final AdapterLog log;
        private ImmutableSet<ContentEquivalenceGenerator<T>> generators = ImmutableSet.of();
        private ImmutableSet<ContentEquivalenceScorer<T>> scorers = ImmutableSet.of();

        public Builder(EquivalenceResultBuilder<T> resultBuilder, AdapterLog log) {
            this.resultBuilder = checkNotNull(resultBuilder);
            this.log = checkNotNull(log);
        }

        public Builder<T> withGenerator(ContentEquivalenceGenerator<T> generator) {
            this.generators = ImmutableSet.of(generator);
            return this;
        }
        
        public Builder<T> withGenerators(Iterable<ContentEquivalenceGenerator<T>> generators) {
            this.generators = ImmutableSet.copyOf(generators);
            return this;
        }
        
        public Builder<T> withScorer(ContentEquivalenceScorer<T> scorer) {
            this.scorers = ImmutableSet.of(scorer);
            return this;
        }
        
        public Builder<T> withScorers(Iterable<ContentEquivalenceScorer<T>> scorers) {
            this.scorers = ImmutableSet.copyOf(scorers);
            return this;
        }
        
        public ItemEquivalenceUpdater<T> build() {
            return new ItemEquivalenceUpdater<T>(generators, scorers, resultBuilder, log);
        }
        
    }
    
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
