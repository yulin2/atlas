package org.atlasapi.equiv.handlers;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public interface EquivalenceResultHandler<T extends Content> {

    void handle(EquivalenceResult<T> result);
    
}
