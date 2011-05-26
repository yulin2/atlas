package org.atlasapi.equiv.results.persistence;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public interface EquivalenceResultStore {

    <T extends Content> RestoredEquivalenceResult store(EquivalenceResult<T> result);
    
    RestoredEquivalenceResult forId(String canonicalUri);
    
    List<RestoredEquivalenceResult> forIds(Iterable<String> canonicalUris);
}
