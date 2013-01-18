package org.atlasapi.equiv.results.persistence;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

public class RecentEquivalenceResultStore implements EquivalenceResultStore {

    private final EquivalenceResultStore delegate;
    private final Cache<Id, StoredEquivalenceResult> mrwItemCache;
    private final Cache<Id, StoredEquivalenceResult> mrwContainerCache;

    public RecentEquivalenceResultStore(EquivalenceResultStore delegate) {
        this.delegate = delegate;
        this.mrwItemCache = CacheBuilder.newBuilder().maximumSize(50).build();
        this.mrwContainerCache = CacheBuilder.newBuilder().maximumSize(50).build();
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
        StoredEquivalenceResult equivalenceResult = mrwItemCache.getIfPresent(id);
        if(equivalenceResult != null) {
            return equivalenceResult;
        }
        
        equivalenceResult = mrwContainerCache.getIfPresent(id);
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
        return ImmutableList.copyOf(mrwItemCache.asMap().values());
    }

    public List<StoredEquivalenceResult> latestContainerResults() {
        return ImmutableList.copyOf(mrwContainerCache.asMap().values());
    }

}
