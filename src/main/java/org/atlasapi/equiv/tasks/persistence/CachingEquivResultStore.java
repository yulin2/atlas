package org.atlasapi.equiv.tasks.persistence;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.media.entity.Described;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;

public class CachingEquivResultStore implements EquivResultStore {

    private final EquivResultStore delegate;
    private ConcurrentMap<String, ContainerEquivResult<String, String>> cache;
    private ContainerEquivResultTransformer<Described, String> transformer = ContainerEquivResultTransformer.defaultTransformer();

    public CachingEquivResultStore(final EquivResultStore delegate) {
        this.delegate = delegate;
        this.cache = new MapMaker().softValues().expireAfterWrite(15, TimeUnit.MINUTES).makeComputingMap(new Function<String, ContainerEquivResult<String, String>>() {
            @Override
            public ContainerEquivResult<String, String> apply(String input) {
                return delegate.resultFor(input);
            }
        });
    }

    @Override
    public <T extends Described, U extends Described> void store(ContainerEquivResult<T, U> result) {
        delegate.store(result);
        this.cache.put(result.described().getCanonicalUri(), transformer.transform(result));
    }
    
    @Override
    public ContainerEquivResult<String, String> resultFor(String canonicalUri) {
        try {
            return cache.get(canonicalUri);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public List<ContainerEquivResult<String, String>> results() {
        return ImmutableList.copyOf(cache.values());
    }

    @Override
    public List<ContainerEquivResult<String, String>> resultsBeginningWith(String prefix) {
        return delegate.resultsBeginningWith(prefix);
    }

}
