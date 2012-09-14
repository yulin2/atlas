package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.content.Content;

public abstract class FoldingEquivalenceCombiner<T extends Content> implements ScoreCombiner<T> {

    @Override
    public ScoredCandidates<T> combine(List<ScoredCandidates<T>> scoredEquivalents, ResultDescription desc) {
        if(scoredEquivalents == null || scoredEquivalents.isEmpty()) {
            return null;
        }
        ScoredCandidates<T> head = scoredEquivalents.get(0);
        for (ScoredCandidates<T> tailElem : scoredEquivalents.subList(1, scoredEquivalents.size())) {
            head = combine(head, tailElem);
        }
        return head;
    }

    protected abstract ScoredCandidates<T> combine(ScoredCandidates<T> head, ScoredCandidates<T> tailElem);

}