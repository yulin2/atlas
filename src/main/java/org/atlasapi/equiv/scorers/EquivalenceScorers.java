package org.atlasapi.equiv.scorers;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EquivalenceScorers<T extends Content> {

    public static <T extends Content> EquivalenceScorers<T> from(Iterable<ContentEquivalenceScorer<T>> generators, AdapterLog log) {
        return new EquivalenceScorers<T>(generators, log);
    }

    private final Iterable<ContentEquivalenceScorer<T>> scorers;
    private final AdapterLog log;

    public EquivalenceScorers(Iterable<ContentEquivalenceScorer<T>> scorers, AdapterLog log) {
        this.scorers = scorers;
        this.log = log;
    }

    public List<ScoredEquivalents<T>> score(T content, List<T> generatedSuggestions, ResultDescription desc) {
        desc.startStage("Scoring equivalences");
        Builder<ScoredEquivalents<T>> scoredScores = ImmutableList.builder();

        for (ContentEquivalenceScorer<T> scorer : scorers) {
            try {
                desc.startStage(scorer.toString());
                scoredScores.add(scorer.score(content, generatedSuggestions, desc));
                desc.finishStage();
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Exception running %s for %s", scorer, content));
                /*
                 * Propagate to make sure the equivalence update for this
                 * content fails - if a scorer fails intermittently there's a
                 * risk of equivalence flip-flop.
                 */
                throw Throwables.propagate(e);
            }
        }

        desc.finishStage();
        return scoredScores.build();
    }

}
