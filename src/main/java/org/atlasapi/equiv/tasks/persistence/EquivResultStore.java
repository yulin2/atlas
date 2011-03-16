package org.atlasapi.equiv.tasks.persistence;

import java.util.List;

import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.media.entity.Described;

public interface EquivResultStore {

    <T extends Described> void store(EquivResult<T> result);
    
    EquivResult<String> resultFor(String canonicalUri);
    
    List<EquivResult<String>> results();
    
}
