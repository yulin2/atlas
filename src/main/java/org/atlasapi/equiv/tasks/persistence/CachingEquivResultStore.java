package org.atlasapi.equiv.tasks.persistence;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.media.entity.Described;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

public class CachingEquivResultStore implements EquivResultStore {

    private static final Function<Described, String> TRANSFORMER = new Function<Described, String>() {
        @Override
        public String apply(Described input) {
            return String.format("%s/%s", input.getTitle(), input.getCanonicalUri());
        }
    };

    private final EquivResultStore delegate;
    private ConcurrentMap<String, ContainerEquivResult<String, String>> cache;

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
        this.cache.put(result.described().getCanonicalUri(), toStringResult(result));
    }

    private <T extends Described, U extends Described> ContainerEquivResult<String, String> toStringResult(ContainerEquivResult<T, U> result) {
        EquivResult<String> baseResult = result.transformResult(TRANSFORMER);
        return new ContainerEquivResult<String, String>(baseResult, Iterables.transform(result.getItemResults(), new Function<EquivResult<U>, EquivResult<String>>() {

            @Override
            public EquivResult<String> apply(EquivResult<U> input) {
                return input.transformResult(TRANSFORMER);
            }
        }));
    }

    @Override
    public ContainerEquivResult<String, String> resultFor(String canonicalUri) {
        return cache.get(canonicalUri);
    }

    @Override
    public List<ContainerEquivResult<String, String>> results() {
        return ImmutableList.copyOf(cache.values());
    }

}
