package org.atlasapi.query.content.search;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.content.Identified;
import org.atlasapi.media.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;

import com.google.common.collect.ImmutableList;

public class DummySearcher implements SearchResolver {
    
    private final Iterable<Identified> respondWith;
    
    public DummySearcher() {
        this(ImmutableList.<Identified>of());
    }

    public DummySearcher(Iterable<Identified> respondWith) {
        this.respondWith = respondWith;
    }

    @Override
    public List<Identified> search(SearchQuery query, ApplicationConfiguration appConfig) {
        return ImmutableList.copyOf(respondWith);
    }
}
