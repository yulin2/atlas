package org.atlasapi.equiv.results;

import org.atlasapi.media.entity.Content;

public interface EquivalenceResultHandler<T extends Content> {

    void handle(EquivalenceResult<T> result);
    
}
