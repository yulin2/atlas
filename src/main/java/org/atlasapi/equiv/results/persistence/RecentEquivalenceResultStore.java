package org.atlasapi.equiv.results.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.ImmutableList;

public class RecentEquivalenceResultStore implements EquivalenceResultStore {

    private static class LimitedSizeResultMap extends LinkedHashMap<String,StoredEquivalenceResult> {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, StoredEquivalenceResult> eldest) {
            return size() > 50;
        }
    }
    
    private final EquivalenceResultStore delegate;
    private final Map<String,StoredEquivalenceResult> mrwItemCache;
    private final Map<String,StoredEquivalenceResult> mrwContainerCache;

    public RecentEquivalenceResultStore(EquivalenceResultStore delegate) {
        this.delegate = delegate;
        this.mrwItemCache = new LimitedSizeResultMap();
        this.mrwContainerCache = new LimitedSizeResultMap();
    }
    
    @Override
    public <T extends Content> StoredEquivalenceResult store(EquivalenceResult<T> result) {
        StoredEquivalenceResult restoredResult = delegate.store(result);
        if(result.subject() instanceof Item) {
            mrwItemCache.put(result.subject().getCanonicalUri(), restoredResult);
        }
        if(result.subject() instanceof Container) {
            mrwContainerCache.put(result.subject().getCanonicalUri(), restoredResult);
        }
        return restoredResult;
    }

    @Override
    public StoredEquivalenceResult forId(String canonicalUri) {
        StoredEquivalenceResult equivalenceResult = mrwItemCache.get(canonicalUri);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        
        equivalenceResult = mrwContainerCache.get(canonicalUri);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        return delegate.forId(canonicalUri);
    }
    
    @Override
    public List<StoredEquivalenceResult> forIds(Iterable<String> canonicalUris) {
        return delegate.forIds(canonicalUris);
    }
    
    public List<StoredEquivalenceResult> latestItemResults() {
        return ImmutableList.copyOf(mrwItemCache.values());
    }

    public List<StoredEquivalenceResult> latestContainerResults() {
        return ImmutableList.copyOf(mrwContainerCache.values());
    }

}
