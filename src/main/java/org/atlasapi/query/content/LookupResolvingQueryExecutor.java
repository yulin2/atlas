package org.atlasapi.query.content;

import java.util.List;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.lookup.LookupResolver;
import org.atlasapi.persistence.lookup.entry.Equivalent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class LookupResolvingQueryExecutor implements KnownTypeQueryExecutor {

    private final KnownTypeQueryExecutor delegate;
    private final LookupResolver lookupResolver;

    public LookupResolvingQueryExecutor(KnownTypeQueryExecutor delegate, LookupResolver lookupResolver) {
        this.delegate = delegate;
        this.lookupResolver = lookupResolver;
    }

    @Override
    public List<Content> discover(ContentQuery query) {
        return delegate.discover(query);
    }

    @Override
    public List<Identified> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
        return ImmutableList.copyOf(Iterables.concat(Iterables.transform(uris, new Function<String, List<Identified>>() {
            @Override
            public List<Identified> apply(String input) {
                Iterable<String> equivUris = Iterables.transform(lookupResolver.lookup(input, query.getConfiguration()), Equivalent.TO_ID);
                if (Iterables.isEmpty(equivUris)) {
                    return ImmutableList.of();
                }
                return setEquivalentToFields(delegate.executeUriQuery(equivUris, query));
            }
        })));
    }

    private List<Identified> setEquivalentToFields(List<Identified> equivItems) {
        ImmutableSet<String> uris = ImmutableSet.copyOf(Iterables.transform(equivItems, Identified.TO_URI));
        for (Identified ided : equivItems) {
            ided.setEquivalentTo(Sets.difference(uris, ImmutableSet.of(ided.getCanonicalUri())));
        }
        return equivItems;
    }
}
