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
import org.atlasapi.query.content.search.ContentResolvingSearcher;
import org.atlasapi.search.ContentSearcher;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

public class ContentResolvingSearchTest extends MockObjectTestCase {
    private final ContentSearcher fuzzySearcher = mock(ContentSearcher.class);
    private final KnownTypeQueryExecutor contentResolver = mock(KnownTypeQueryExecutor.class);
    
    private final Brand brand = new Brand("brand", "brand", Publisher.BBC);
    private final Episode item = new Episode("item", "item", Publisher.BBC);
    private final Selection selection = new Selection(0, 10);
    private final List<Publisher> publishers = ImmutableList.of(Publisher.BBC);
    
    private ContentResolvingSearcher searcher;

    @Override
    protected void setUp() throws Exception {
        brand.setChildRefs(ImmutableList.of(item.childRef()));
        searcher = new ContentResolvingSearcher(fuzzySearcher, contentResolver);
    }
    
    public void testShouldReturnSearchedForItem() {
        final String searchQuery = "test";
        final ContentQuery contentQuery = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Enum<Publisher>>copyOf(publishers)).withSelection(selection).build();
        final SearchQuery query = new SearchQuery(searchQuery, selection, publishers, 1.0f, 0.0f, 0.0f);
        
        checking(new Expectations() {{ 
            one(fuzzySearcher).search(query); will(returnValue(new SearchResults(ImmutableList.of(brand.getCanonicalUri()))));
            one(contentResolver).executeUriQuery(ImmutableList.of(brand.getCanonicalUri()), contentQuery); will(returnValue(ImmutableMap.of(brand.getCanonicalUri(), ImmutableList.of(brand))));
        }});
            
        List<Identified> content = searcher.search(query, ApplicationConfiguration.DEFAULT_CONFIGURATION);
        assertFalse(content.isEmpty());
        Brand result = (Brand) Iterables.getOnlyElement(content);
        assertTrue(result.getChildRefs().isEmpty());
        assertTrue(result.getClips().isEmpty());
    }
}
