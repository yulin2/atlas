package org.atlasapi.query.v4.search.support;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.ContentSearcher;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;

import com.google.common.collect.ImmutableList;

public class ContentResolvingSearcher implements SearchResolver {

    private final ContentSearcher searcher;
    private final ContentResolver contentResolver;
    private final long timeout;

    public ContentResolvingSearcher(ContentSearcher searcher, ContentResolver contentResolver, long timeout) {
        this.searcher = checkNotNull(searcher);
        this.contentResolver = checkNotNull(contentResolver);
        this.timeout = timeout;
    }

    @Override
    public List<Identified> search(SearchQuery query, ApplicationConfiguration appConfig) {
        try {
            SearchResults searchResults = searcher.search(query).get(timeout, TimeUnit.MILLISECONDS);

            List<Id> ids = searchResults.getIds();
            if (ids.isEmpty()) {
                return ImmutableList.of();
            }

            return ImmutableList.<Identified>copyOf(contentResolver.resolveIds(ids).getResources());
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
