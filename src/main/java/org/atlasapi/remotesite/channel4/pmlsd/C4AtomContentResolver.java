package org.atlasapi.remotesite.channel4.pmlsd;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class C4AtomContentResolver {

    private final ContentResolver resolver;

    public C4AtomContentResolver(ContentResolver resolver) {
        this.resolver = resolver;
    }
    
    public Optional<Item> itemFor(String id) {
        List<Identified> results = resolver.findByCanonicalUris(Lists.newArrayList(id)).getAllResolvedResults();
        Iterable<Item> itemFilteredResults = ImmutableSet.copyOf(Iterables.filter(results, Item.class));
        return Optional.fromNullable(Iterables.getOnlyElement(itemFilteredResults,null));
    }
    
    public Optional<Brand> brandFor(String canonicalUri) {
        Maybe<Identified> result = resolver.findByCanonicalUris(ImmutableList.of(canonicalUri)).get(canonicalUri);
        if (result.hasValue() && result.requireValue() instanceof Brand) {
            return Optional.of((Brand)result.requireValue());
        }
        return Optional.absent();
    }
    
    public Optional<Series> seriesFor(String canonicalUri) {
        Maybe<Identified> result = resolver.findByCanonicalUris(ImmutableList.of(canonicalUri)).get(canonicalUri);
        if (result.hasValue() && result.requireValue() instanceof Series) {
            return Optional.of((Series)result.requireValue());
        }
        return Optional.absent();
    }
}
