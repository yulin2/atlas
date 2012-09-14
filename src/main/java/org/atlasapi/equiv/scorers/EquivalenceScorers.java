package org.atlasapi.equiv.scorers;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EquivalenceScorers<T extends Content> {
    
    public static <T extends Content> EquivalenceScorers<T> from(Iterable<EquivalenceScorer<T>> generators) {
        return new EquivalenceScorers<T>(generators);
    }

    private final Iterable<EquivalenceScorer<T>> scorers;

    public EquivalenceScorers(Iterable<EquivalenceScorer<T>> scorers) {
        this.scorers = scorers;
    }

    public List<ScoredCandidates<T>> score(T content, List<T> generatedSuggestions, ResultDescription desc) {
        desc.startStage("Scoring equivalences");
        Builder<ScoredCandidates<T>> scoredScores = ImmutableList.builder();

        for (EquivalenceScorer<T> scorer : scorers) {
            try {
                desc.startStage(scorer.toString());
                scoredScores.add(scorer.score(content, generatedSuggestions, desc));
                desc.finishStage();
            } catch (Exception e) {
                throw new RuntimeException(String.format("{} - {}", scorer, content), e);
            }
        }

        desc.finishStage();
        return scoredScores.build();
    }

}
