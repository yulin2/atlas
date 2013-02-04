package org.atlasapi.query.content;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.query.content.search.ContentResolvingSearcher;
import org.atlasapi.search.ContentSearcher;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;

@RunWith(MockitoJUnitRunner.class)
public class ContentResolvingSearchTest extends TestCase {
    
    private final ContentSearcher fuzzySearcher = mock(ContentSearcher.class);
    private final KnownTypeQueryExecutor contentResolver = mock(KnownTypeQueryExecutor.class);
    
    private final ContentResolvingSearcher searcher = new ContentResolvingSearcher(fuzzySearcher, contentResolver);

    @Override
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testShouldReturnSearchedForItem() {
        Brand brand = new Brand("brand", "brand", Publisher.BBC);
        brand.setId(1234L);
        Episode item = new Episode("item", "item", Publisher.BBC);
        brand.setChildRefs(ImmutableList.of(item.childRef()));

        Selection selection = new Selection(0, 10);
        List<Publisher> publishers = ImmutableList.of(Publisher.BBC);

        String searchQuery = "test";
        ContentQuery contentQuery = ContentQueryBuilder.query().isAnEnumIn(Attributes.DESCRIPTION_PUBLISHER, ImmutableList.<Publisher>copyOf(publishers)).withSelection(selection).build();
        SearchQuery query = new SearchQuery(searchQuery, selection, publishers, 1.0f, 0.0f, 0.0f);
        
        when(fuzzySearcher.search(query)).thenReturn(new SearchResults(ImmutableList.of(brand.getId())));
        when(contentResolver.executeIdQuery(ImmutableList.of(brand.getId()), contentQuery)).thenReturn(ImmutableMap.<Id,List<Identified>>of(brand.getId(), ImmutableList.<Identified>of(brand)));
            
        List<Identified> content = searcher.search(query, ApplicationConfiguration.DEFAULT_CONFIGURATION);
        assertFalse(content.isEmpty());
        Brand result = (Brand) Iterables.getOnlyElement(content);
        assertFalse(result.getChildRefs().isEmpty());
        assertTrue(result.getClips().isEmpty());
    }
}
