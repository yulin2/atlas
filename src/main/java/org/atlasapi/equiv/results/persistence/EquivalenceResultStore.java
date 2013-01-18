package org.atlasapi.equiv.results.persistence;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;

public interface EquivalenceResultStore {

    <T extends Content> StoredEquivalenceResult store(EquivalenceResult<T> result);
    
    StoredEquivalenceResult forId(Id id);
    
    List<StoredEquivalenceResult> forIds(Iterable<Id> ids);
}
