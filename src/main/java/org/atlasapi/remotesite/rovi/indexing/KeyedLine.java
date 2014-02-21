package org.atlasapi.remotesite.rovi.indexing;


/**
 * Represents the parsed version of a line in a text file. The line contains a field that can be used as a key in order to index the file
 *
 * @param <T> - The type of the key
 */
public interface KeyedLine<T> {

    
    /**
     * @return the value of the key for this line
     */
    T getKey();
    
}
