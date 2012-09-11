package org.atlasapi.query.v4.search.support;

import java.util.List;
import java.util.Map;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.metabroadcast.common.collect.DedupingIterator;
import java.util.concurrent.TimeUnit;
import org.atlasapi.persistence.content.ContentSearcher;

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
            List<String> uris = searchResults.toUris();
            if (uris.isEmpty()) {
                return ImmutableList.of();
            }

            ContentQuery contentQuery = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Enum<Publisher>>copyOf(query.getIncludedPublishers())).withSelection(query.getSelection()).build();
            Map<String, List<Identified>> content = resolver.executeUriQuery(uris, contentQuery.copyWithApplicationConfiguration(appConfig));

            List<Identified> hydrated = Lists.newArrayListWithExpectedSize(uris.size());
            for (String uri : uris) {
                List<Identified> identified = content.get(uri);
                if (identified != null) {
                    hydrated.addAll(identified);
                }
            }

            return DedupingIterator.dedupeIterable(hydrated);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
