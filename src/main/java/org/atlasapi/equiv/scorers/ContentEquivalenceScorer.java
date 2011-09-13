package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

public interface ContentEquivalenceScorer<T extends Content> {

    ScoredEquivalents<T> score(T content, Iterable<T> suggestions, ResultDescription desc);
    
}
