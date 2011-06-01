package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public interface ContentEquivalenceUpdater<T extends Content> {

    EquivalenceResult<T> updateEquivalences(T content);
    
}
