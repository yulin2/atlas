package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Content;

public interface ContentEquivalenceGenerator<T extends Content> {

    ScoredEquivalents<T> generate(T content, ResultDescription desc);
    
}
