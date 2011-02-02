package org.atlasapi.query.content.fuzzy;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.search.model.SearchResults;

import com.metabroadcast.common.query.Selection;

public interface FuzzySearcher {

	SearchResults contentSearch(String title, Selection selection, Iterable<Publisher> publishers);

}
