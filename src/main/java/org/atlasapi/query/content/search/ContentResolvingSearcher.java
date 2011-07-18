package org.atlasapi.query.content.search;

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
import org.atlasapi.search.ContentSearcher;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ContentResolvingSearcher implements SearchResolver {
    private final ContentSearcher fuzzySearcher;
    private final KnownTypeQueryExecutor contentResolver;

    public ContentResolvingSearcher(ContentSearcher fuzzySearcher, KnownTypeQueryExecutor contentResolver) {
        this.fuzzySearcher = fuzzySearcher;
        this.contentResolver = contentResolver;
    }

    @Override
    public List<Identified> search(SearchQuery query, ApplicationConfiguration appConfig) {
        SearchResults searchResults = fuzzySearcher.search(query);
        if (searchResults.toUris().isEmpty()) {
            return ImmutableList.of();
        }

        ContentQuery contentQuery = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Enum<Publisher>> copyOf(query.getIncludedPublishers()))
                .withSelection(query.getSelection()).build();
        Map<String, List<Identified>> content = contentResolver.executeUriQuery(searchResults.toUris(), contentQuery.copyWithApplicationConfiguration(appConfig));
        return ImmutableList.copyOf(Iterables.concat(content.values()));
    }

}
