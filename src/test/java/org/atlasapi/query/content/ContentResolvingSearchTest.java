package org.atlasapi.query.content;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.query.content.fuzzy.FuzzySearcher;
import org.atlasapi.query.content.search.ContentResolvingSearcher;
import org.atlasapi.search.model.Search;
import org.atlasapi.search.model.SearchResults;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

public class ContentResolvingSearchTest extends MockObjectTestCase {
    private final FuzzySearcher fuzzySearcher = mock(FuzzySearcher.class);
    private final KnownTypeQueryExecutor contentResolver = mock(KnownTypeQueryExecutor.class);
    
    private final Brand brand = new Brand("brand", "brand", Publisher.BBC);
    private final Episode item = new Episode("item", "item", Publisher.BBC);
    private final Selection selection = new Selection(0, 10);
    private final List<Publisher> publishers = ImmutableList.of(Publisher.BBC);
    
    private ContentResolvingSearcher searcher;

    @Override
    protected void setUp() throws Exception {
        brand.addContents(item);
        
        searcher = new ContentResolvingSearcher(fuzzySearcher, contentResolver);
    }
    
    public void testShouldReturnSearchedForItem() {
        final String searchQuery = "test";
        final ContentQuery query = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Enum<Publisher>>copyOf(publishers)).withSelection(selection).build();
        
        checking(new Expectations() {{ 
            one(fuzzySearcher).contentSearch(searchQuery, selection, publishers); will(returnValue(new SearchResults(ImmutableList.of(brand.getCanonicalUri()))));
            one(contentResolver).executeUriQuery(ImmutableList.of(brand.getCanonicalUri()), query); will(returnValue(ImmutableList.of(brand)));
        }});
        
        List<Identified> content = searcher.search(new Search(searchQuery), publishers, ApplicationConfiguration.DEFAULT_CONFIGURATION, selection);
        assertFalse(content.isEmpty());
        Brand result = (Brand) Iterables.getOnlyElement(content);
        assertTrue(result.getContents().isEmpty());
        assertTrue(result.getClips().isEmpty());
    }
}
