package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

public interface ContentEquivalenceGenerator<T extends Content> {

    ScoredEquivalents<T> generate(T content);
    
}
