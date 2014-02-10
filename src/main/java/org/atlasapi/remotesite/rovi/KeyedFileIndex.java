package org.atlasapi.remotesite.rovi;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Represents the index of a text file. Every line should contain a key that identifies the line, even if not uniquely (multiple lines could have the same key). 
 * The index maps a key of type {@code T} with one more lines of type {@code S}. {@code S} is the type of the line already parsed into the specific model.
 *
 * @param <T> The type of the key
 * @param <S> The type of the parsed line
 */
public interface KeyedFileIndex<T, S extends KeyedLine<T>> {


    /**
     * @param key - the key of the lines
     * @return a collection of the lines in the file identified by the key
     * @throws IOException if it's not possible to read the file
     */
    Collection<S> getLinesForKey(T key) throws IOException;

    /**
     * @return the set of the keys in this index
     */
    Set<T> getKeys();
    
}
