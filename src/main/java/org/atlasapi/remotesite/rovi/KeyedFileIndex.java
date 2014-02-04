package org.atlasapi.remotesite.rovi;

import java.io.IOException;
import java.util.Collection;


public interface KeyedFileIndex<T, S extends KeyedLine<T>> {

    Collection<S> getLinesForKey(T key) throws IOException;
    
}
