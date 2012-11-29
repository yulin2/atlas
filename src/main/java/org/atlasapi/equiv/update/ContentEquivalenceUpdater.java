package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Set;

import org.atlasapi.equiv.generators.EquivalenceGenerator;
import org.atlasapi.equiv.generators.EquivalenceGenerators;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.DefaultEquivalenceResultBuilder;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.combining.ScoreCombiner;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.filters.EquivalenceFilter;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.equiv.results.scores.ScoredEquivalentsMerger;
import org.atlasapi.equiv.scorers.EquivalenceScorer;
import org.atlasapi.equiv.scorers.EquivalenceScorers;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ContentEquivalenceUpdater<T extends Content> implements EquivalenceUpdater<T> {

    private final ScoredEquivalentsMerger merger = new ScoredEquivalentsMerger();
    private final Function<ScoredCandidates<T>, Iterable<T>> extractCandidates = new Function<ScoredCandidates<T>, Iterable<T>>() {
        @Override
        public Iterable<T> apply(ScoredCandidates<T> input) {
            return input.candidates().keySet();
        }
    };
    
    private final EquivalenceGenerators<T> generators;
    private final EquivalenceScorers<T> scorers;
    private final DefaultEquivalenceResultBuilder<T> resultBuilder;
    private final EquivalenceResultHandler<T> handler;
    
    public ContentEquivalenceUpdater(
        Iterable<? extends EquivalenceGenerator<T>> generators,
        Iterable<? extends EquivalenceScorer<T>> scorers,
        ScoreCombiner<T> combiner,
        EquivalenceFilter<T> filter,
        EquivalenceExtractor<T> extractor,
        EquivalenceResultHandler<T> handler
    ) {
        this.generators = EquivalenceGenerators.from(generators);
        this.scorers = EquivalenceScorers.from(scorers);
        this.resultBuilder = new DefaultEquivalenceResultBuilder<T>(combiner, filter, extractor);
        this.handler = handler;
    }

    @Override
    public void updateEquivalences(T content) {

        ReadableDescription desc = new DefaultDescription();
        
        List<ScoredCandidates<T>> generatedScores = generators.generate(content, desc);
        
        Set<T> candidates = ImmutableSet.copyOf(extractCandidates(generatedScores));
        
        List<ScoredCandidates<T>> scoredScores = scorers.score(content, candidates, desc);
        
        List<ScoredCandidates<T>> mergedScores = merger.merge(generatedScores, scoredScores);
        
        EquivalenceResult<T> result = resultBuilder.resultFor(content, mergedScores, desc);
        
        handler.handle(result);
    }
    
    private Iterable<T> extractCandidates(Iterable<ScoredCandidates<T>> generatedScores) {
        return Iterables.concat(Iterables.transform(generatedScores, extractCandidates));
    }
}
