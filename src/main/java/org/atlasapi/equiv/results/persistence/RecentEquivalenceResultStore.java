package org.atlasapi.equiv.results.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.ImmutableList;

public class RecentEquivalenceResultStore implements EquivalenceResultStore {

    private static class LimitedSizeResultMap extends LinkedHashMap<String,RestoredEquivalenceResult> {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, RestoredEquivalenceResult> eldest) {
            return size() > 50;
        }
    }
    
    private final EquivalenceResultStore delegate;
    private final Map<String,RestoredEquivalenceResult> mrwItemCache;
    private final Map<String,RestoredEquivalenceResult> mrwContainerCache;

    public RecentEquivalenceResultStore(EquivalenceResultStore delegate) {
        this.delegate = delegate;
        this.mrwItemCache = new LimitedSizeResultMap();
        this.mrwContainerCache = new LimitedSizeResultMap();
    }
    
    @Override
    public <T extends Content> RestoredEquivalenceResult store(EquivalenceResult<T> result) {
        RestoredEquivalenceResult restoredResult = delegate.store(result);
        if(result.target() instanceof Item) {
            mrwItemCache.put(result.target().getCanonicalUri(), restoredResult);
        }
        if(result.target() instanceof Container) {
            mrwContainerCache.put(result.target().getCanonicalUri(), restoredResult);
        }
        return restoredResult;
    }

    @Override
    public RestoredEquivalenceResult forId(String canonicalUri) {
        RestoredEquivalenceResult equivalenceResult = mrwItemCache.get(canonicalUri);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        
        equivalenceResult = mrwContainerCache.get(canonicalUri);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        return delegate.forId(canonicalUri);
    }
    
    public List<RestoredEquivalenceResult> latestItemResults() {
        return ImmutableList.copyOf(mrwItemCache.values());
    }

    public List<RestoredEquivalenceResult> latestContainerResults() {
        return ImmutableList.copyOf(mrwContainerCache.values());
    }
}
