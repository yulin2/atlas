package org.atlasapi.equiv.results.extractors;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

public interface EquivalenceFilter<T extends Content> {
    
    boolean apply(ScoredEquivalent<T> input, T subject);
    
}
