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
import org.atlasapi.query.content.fuzzy.FuzzySearcher;
import org.atlasapi.search.model.Search;
import org.atlasapi.search.model.SearchResults;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

public class ContentResolvingSearcher implements SearchResolver {
    private final FuzzySearcher fuzzySearcher;
    private final KnownTypeQueryExecutor contentResolver;

    public ContentResolvingSearcher(FuzzySearcher fuzzySearcher, KnownTypeQueryExecutor contentResolver) {
        this.fuzzySearcher = fuzzySearcher;
        this.contentResolver = contentResolver;
    }

    @Override
    public List<Identified> search(Search search, Iterable<Publisher> publishers, ApplicationConfiguration appConfig, Selection selection) {
        SearchResults searchResults = fuzzySearcher.contentSearch(search.query(), selection, publishers);
        if (searchResults.toUris().isEmpty()) {
            return ImmutableList.of();
        }

        ContentQuery query = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Enum<Publisher>> copyOf(publishers)).withSelection(selection).build();
        Map<String, List<Identified>> content = contentResolver.executeUriQuery(searchResults.toUris(), query.copyWithApplicationConfiguration(appConfig));
        return ImmutableList.copyOf(Iterables.concat(content.values()));
    }

}
