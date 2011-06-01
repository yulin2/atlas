package org.atlasapi.equiv.results.combining;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

public interface EquivalenceCombiner<T extends Content> {

    ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents);
    
}
