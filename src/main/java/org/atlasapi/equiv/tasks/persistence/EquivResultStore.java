package org.atlasapi.equiv.tasks.persistence;

import java.util.List;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.media.entity.Described;

public interface EquivResultStore {

    <T extends Described, U extends Described> void store(ContainerEquivResult<T, U> result);
    
    ContainerEquivResult<String, String> resultFor(String canonicalUri);
    
    List<ContainerEquivResult<String, String>> results();

    
}
