package org.atlasapi.remotesite.rovi.indexing;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Predicate;


/**
 * Instances of this class are able to create a {@link KeyedFileIndex} for a text file
 *
 * @param <T> - The type of the key
 * @param <S> - The type of the line parsed
 */
public interface KeyedFileIndexer<T, S extends KeyedLine<T>> {

    
    /**
     * @return the index for a text file
     * @throws IOException if it's not possible to read the file
     */
    KeyedFileIndex<T, S> index(File file) throws IOException;

    /**
     * @param predicate - a predicate that defines if the record must be indexed or not
     * @return the index for a text file
     * @throws IOException if it's not possible to read the file
     */
    KeyedFileIndex<T, S> index(File file, Predicate<? super S> isToIndex) throws IOException;
    
}
