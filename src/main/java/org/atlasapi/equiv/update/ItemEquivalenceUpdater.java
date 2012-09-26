package org.atlasapi.equiv.update;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.equiv.generators.EquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.EquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Deprecated
public class ItemEquivalenceUpdater<T extends Item> implements EquivalenceUpdater<T> {

    public static <T extends Item> Builder<T> builder(EquivalenceResultBuilder<T> resultBuilder) {
        return new Builder<T>(resultBuilder);
    }
    
    public static class Builder<T extends Item> {
        
        private final EquivalenceResultBuilder<T> resultBuilder;
        private ImmutableSet<EquivalenceGenerator<T>> generators = ImmutableSet.of();
        private ImmutableSet<EquivalenceScorer<T>> scorers = ImmutableSet.of();

        public Builder(EquivalenceResultBuilder<T> resultBuilder) {
            this.resultBuilder = checkNotNull(resultBuilder);
        }

        public Builder<T> withGenerator(EquivalenceGenerator<T> generator) {
            this.generators = ImmutableSet.of(generator);
            return this;
        }
        
        public Builder<T> withGenerators(Iterable<EquivalenceGenerator<T>> generators) {
            this.generators = ImmutableSet.copyOf(generators);
            return this;
        }
        
        public Builder<T> withScorer(EquivalenceScorer<T> scorer) {
            this.scorers = ImmutableSet.of(scorer);
            return this;
        }
        
        public Builder<T> withScorers(Iterable<EquivalenceScorer<T>> scorers) {
            this.scorers = ImmutableSet.copyOf(scorers);
            return this;
        }
        
        public ItemEquivalenceUpdater<T> build() {
            return new ItemEquivalenceUpdater<T>(generators, scorers, resultBuilder);
        }
        
    }
    
    private final EquivalenceGenerators<T> generators;
    private final EquivalenceScorers<T> scorers;
    private final EquivalenceResultBuilder<T> resultBuilder;

    private final ScoredEquivalentsMerger merger = new ScoredEquivalentsMerger();
    private final List<T> NONE = ImmutableList.of();
    private final Function<ScoredCandidates<T>, Iterable<T>> extractCandidates = new Function<ScoredCandidates<T>, Iterable<T>>() {
        @Override
        public Iterable<T> apply(ScoredCandidates<T> input) {
            return input.candidates().keySet();
        }
    };

    public ItemEquivalenceUpdater(Iterable<EquivalenceGenerator<T>> generators, Iterable<EquivalenceScorer<T>> scorers, EquivalenceResultBuilder<T> resultBuilder) {
        this.generators = EquivalenceGenerators.from(generators);
        this.scorers = EquivalenceScorers.from(scorers);
        this.resultBuilder = resultBuilder;
    }
    
    @Override
    public void updateEquivalences(T content) {
        
        ReadableDescription desc = new DefaultDescription();
        
        List<ScoredCandidates<T>> generatedScores = generators.generate(content, desc);
        
        List<T> suggestions = ImmutableList.<T>builder()
                .addAll(extractCandidates(generatedScores))
//                .addAll(externalCandidates.or(NONE))
                .build();
        
        List<ScoredCandidates<T>> scoredScores = scorers.score(content, suggestions, desc);
        
        resultBuilder.resultFor(content, ImmutableList.copyOf(merger.merge(generatedScores, scoredScores)), desc);
    }

    private Iterable<T> extractCandidates(Iterable<ScoredCandidates<T>> generatedScores) {
        return Iterables.concat(Iterables.transform(generatedScores, extractCandidates));
    }

}
