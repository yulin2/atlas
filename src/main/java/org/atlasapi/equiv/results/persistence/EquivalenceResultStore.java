package org.atlasapi.equiv.results.persistence;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public interface EquivalenceResultStore {

    <T extends Content> void store(EquivalenceResult<T> result);
    
    RestoredEquivalenceResult forId(String canonicalUri);
}
