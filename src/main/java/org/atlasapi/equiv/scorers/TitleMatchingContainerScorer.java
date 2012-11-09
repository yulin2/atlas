package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.generators.ContentTitleScorer;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Container;

import com.google.common.base.Functions;

public class TitleMatchingContainerScorer implements EquivalenceScorer<Container> {

    private ContentTitleScorer<Container> scorer;

    public TitleMatchingContainerScorer() {
        this.scorer = new ContentTitleScorer<Container>(Functions.<String>identity());
    }
    
    @Override
    public ScoredCandidates<Container> score(Container subject, Iterable<Container> candidates, ResultDescription desc) {
        return scorer.scoreCandidates(subject, candidates, desc);
    }

    @Override
    public String toString() {
        return "Title-matching Scorer";
    }
}
