package org.atlasapi.equiv.results.persistence;

import java.util.concurrent.ConcurrentMap;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;

/* Temporary cache for child results. 
 * To avoid size growing indefinitely results are removed when they're read. Not a problem 
 * since children only have one top-level parent.
 */
public class InMemoryLiveEquivalenceResultStore implements LiveEquivalenceResultStore {

    private ConcurrentMap<String, EquivalenceResult<Item>> cache;

    public InMemoryLiveEquivalenceResultStore() {
        this.cache = Maps.newConcurrentMap();
    }
    
    @Override
    public EquivalenceResult<Item> store(EquivalenceResult<Item> result) {
        cache.put(result.subject().getCanonicalUri(), result);
        return result;
    }

    @Override
    public Iterable<EquivalenceResult<Item>> resultsFor(Iterable<String> uris) {
        Builder<EquivalenceResult<Item>> results = ImmutableList.builder();
        for (String uri : uris) {
            EquivalenceResult<Item> result = cache.remove(uri);
            if (result != null) {
                results.add(result);
            }
        }
        return results.build();
    }

}
