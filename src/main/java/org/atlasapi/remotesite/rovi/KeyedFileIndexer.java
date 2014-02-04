package org.atlasapi.remotesite.rovi;

import java.io.IOException;


public interface KeyedFileIndexer<T, S extends KeyedLine<T>> {

    KeyedFileIndex<T, S> index() throws IOException;
    
}
