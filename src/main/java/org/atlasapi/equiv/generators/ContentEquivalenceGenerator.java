package org.atlasapi.equiv.generators;

import java.util.Set;

import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

public interface ContentEquivalenceGenerator<T extends Content> {

    ScoredEquivalents<T> generateEquivalences(T content, Set<T> suggestions);
    
}
