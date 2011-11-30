package org.atlasapi.equiv.results.persistence;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Item;

/**
 * For storage of 'live', as opposed to 'restored', equivalence results.
 * 
 * @author Fred van den Driessche (fred@metabroadcast.com)
 *
 */
public interface LiveEquivalenceResultStore {

    /**
     * Stores an equivalence for later retrieval.
     * @param result - the result to store.
     * @return the result, unchanged
     */
    EquivalenceResult<Item> store(EquivalenceResult<Item>  result);
    
    /**
     * Retrieval previously stored results.
     * @param uris - the URIs of the target content of the desired results.
     * @return the resolved equivalence results of the requested URIs. 
     */
     Iterable<EquivalenceResult<Item>> resultsFor(Iterable<String> uris);
    
}
