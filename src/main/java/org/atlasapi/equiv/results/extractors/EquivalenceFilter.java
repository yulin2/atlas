package org.atlasapi.equiv.results.extractors;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

public interface EquivalenceFilter<T extends Content> {
    
    boolean apply(ScoredEquivalent<T> input, T subject, ResultDescription desc);
    
}
