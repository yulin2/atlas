package org.atlasapi.equiv.results.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.ImmutableList;

public class RecentEquivalenceResultStore implements EquivalenceResultStore {

    private static class LimitedSizeResultMap extends LinkedHashMap<Id,StoredEquivalenceResult> {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<Id, StoredEquivalenceResult> eldest) {
            return size() > 50;
        }
    }
    
    private final EquivalenceResultStore delegate;
    private final Map<Id,StoredEquivalenceResult> mrwItemCache;
    private final Map<Id,StoredEquivalenceResult> mrwContainerCache;

    public RecentEquivalenceResultStore(EquivalenceResultStore delegate) {
        this.delegate = delegate;
        this.mrwItemCache = new LimitedSizeResultMap();
        this.mrwContainerCache = new LimitedSizeResultMap();
    }
    
    @Override
    public <T extends Content> StoredEquivalenceResult store(EquivalenceResult<T> result) {
        StoredEquivalenceResult restoredResult = delegate.store(result);
        if(result.subject() instanceof Item) {
            mrwItemCache.put(result.subject().getId(), restoredResult);
        }
        if(result.subject() instanceof Container) {
            mrwContainerCache.put(result.subject().getId(), restoredResult);
        }
        return restoredResult;
    }

    @Override
    public StoredEquivalenceResult forId(Id id) {
        StoredEquivalenceResult equivalenceResult = mrwItemCache.get(id);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        
        equivalenceResult = mrwContainerCache.get(id);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        return delegate.forId(id);
    }
    
    @Override
    public List<StoredEquivalenceResult> forIds(Iterable<Id> ids) {
        return delegate.forIds(ids);
    }
    
    public List<StoredEquivalenceResult> latestItemResults() {
        return ImmutableList.copyOf(mrwItemCache.values());
    }

    public List<StoredEquivalenceResult> latestContainerResults() {
        return ImmutableList.copyOf(mrwContainerCache.values());
    }

}
