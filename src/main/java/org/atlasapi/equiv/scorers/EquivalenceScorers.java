package org.atlasapi.equiv.scorers;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.collect.Lists;

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
        List<ScoredEquivalents<T>> scoredScores = Lists.newArrayList();

        desc.startStage("Scoring equivalences");
        
        for (ContentEquivalenceScorer<T> scorer : scorers) {
            
            try {
                scoredScores.add(scorer.score(content, generatedSuggestions, desc));
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription(
                        "Exception running scorer %s for %s %s", scorer.getClass().getSimpleName(), content.getClass().getSimpleName(), content.getCanonicalUri()
                ));
            }
            
        }
        
        desc.finishStage();
        
        return scoredScores;
    }
    
    
}
