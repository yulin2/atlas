package org.atlasapi.remotesite.rovi;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;


public interface KeyedFileIndex<T, S extends KeyedLine<T>> {

    Collection<S> getLinesForKey(T key) throws IOException;

    Set<T> getKeys();
    
}
