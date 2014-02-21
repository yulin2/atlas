package org.atlasapi.remotesite.rovi;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

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
     * @throws IndexAccessException if it's not possible to access the index
     */
    Collection<S> getLinesForKey(T key) throws IndexAccessException;

    /**
     * @param key - the key of the lines
     * @param predicate - a predicate used for filtering the set of lines to return
     * @return a collection of the lines in the file identified by the key and that satisfies the predicate
     * @throws IndexAccessException if it's not possible to access the index
     */
    Collection<S> getLinesForKey(T key, Predicate<? super S> predicate) throws IndexAccessException;

    /**
     * Returns the first result in the index for a given key, if exists
     * 
     * @param key - the key of the line
     * @return an optional of the result
     * @throws IndexAccessException
     */
    Optional<S> getFirstForKey(T key) throws IndexAccessException;

    /**
     * @return the set of the keys in this index
     */
    Set<T> getKeys();
    
    /**
     * The index releases the resources it's handling.
     */
    void releaseResources();
    
}
