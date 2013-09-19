package org.atlasapi.equiv.scorers;

import java.util.Set;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.DefaultScoredCandidates.Builder;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Series;

import com.google.common.collect.Iterables;


public class SequenceContainerScorer implements EquivalenceScorer<Container> {

    @Override
    public ScoredCandidates<Container> score(Container subject,
            Set<? extends Container> candidates, ResultDescription desc) {
        Builder<Container> scores = DefaultScoredCandidates.fromSource("Sequence");
        
        if (!(subject instanceof Series)) {
            desc.appendText("subject %s not Series", subject.getClass());
            return scoreAllNull(scores, candidates); 
        }
        
        Series series = (Series) subject;
        if (series.getParent() == null) {
            desc.appendText("subject is top level");
            return scoreAllNull(scores, candidates); 
        }

        if (series.getSeriesNumber() == null) {
            desc.appendText("subject has no series number");
            return scoreAllNull(scores, candidates);
        }
        desc.appendText("subject series number: ", series.getSeriesNumber());
                    
        for (Series candidate : Iterables.filter(candidates, Series.class)) {
            Score score;
            if (candidate.getParent() == null) {
                score = Score.ZERO;
                desc.appendText("%s: top-level: %s", candidate, score);
            } else if (candidate.getSeriesNumber() == null) {
                score = Score.nullScore();
                desc.appendText("%s: no series number: %s", candidate, score);
            } else if (series.getSeriesNumber().equals(candidate.getSeriesNumber())) {
                score = Score.ONE;
                desc.appendText("%s: series number: %s: %s", candidate, candidate.getSeriesNumber(), score);
            } else {
                score = Score.ZERO;
                desc.appendText("%s: series number: %s: %s", candidate, candidate.getSeriesNumber(), score);
            }
            scores.addEquivalent(candidate, score);
        }

        return scores.build();
    }

    private ScoredCandidates<Container> scoreAllNull(Builder<Container> scores,
            Set<? extends Container> candidates) {
        for (Container container : candidates) {
            scores.addEquivalent(container, Score.nullScore());
        }
        return scores.build();
    }

    @Override
    public String toString() {
        return "Container sequence scorer";
    }
    
}
