package org.atlasapi.query.v4.search.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentSearcher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ContentResolvingSearcher implements SearchResolver {

    private final ContentSearcher searcher;
    private final KnownTypeQueryExecutor resolver;
    private final long timeout;

    public ContentResolvingSearcher(ContentSearcher searcher, KnownTypeQueryExecutor resolver, long timeout) {
        this.searcher = searcher;
        this.resolver = resolver;
        this.timeout = timeout;
    }

    @Override
    public List<Identified> search(SearchQuery query, ApplicationConfiguration appConfig) {
        try {
            SearchResults searchResults = searcher.search(query).get(timeout, TimeUnit.MILLISECONDS);
            List<Long> ids = searchResults.getIds();
            if (ids.isEmpty()) {
                return ImmutableList.of();
            }

            ContentQuery contentQuery = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Enum<Publisher>>copyOf(query.getIncludedPublishers())).withSelection(query.getSelection()).build();
            Map<Long, List<Identified>> content = resolver.executeIdQuery(ids, contentQuery.copyWithApplicationConfiguration(appConfig));

            return ImmutableSet.copyOf(Iterables.concat(content.values())).asList();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
