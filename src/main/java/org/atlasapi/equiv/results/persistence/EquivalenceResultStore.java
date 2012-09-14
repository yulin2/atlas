package org.atlasapi.equiv.results.persistence;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.content.Content;

public interface EquivalenceResultStore {

    <T extends Content> StoredEquivalenceResult store(EquivalenceResult<T> result);
    
    StoredEquivalenceResult forId(String canonicalUri);
    
    List<StoredEquivalenceResult> forIds(Iterable<String> canonicalUris);
}
