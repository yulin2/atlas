package org.atlasapi.query.content.search;

import java.util.List;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.Search;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.query.Selection;

public class DummySearcher implements SearchResolver {
    
    private final Iterable<Identified> respondWith;
    
    public DummySearcher() {
        this(ImmutableList.<Identified>of());
    }

    public DummySearcher(Iterable<Identified> respondWith) {
        this.respondWith = respondWith;
    }

    @Override
    public List<Identified> search(Search arg0, Iterable<Publisher> arg1, Selection arg2) {
        return ImmutableList.copyOf(respondWith);
    }
}
