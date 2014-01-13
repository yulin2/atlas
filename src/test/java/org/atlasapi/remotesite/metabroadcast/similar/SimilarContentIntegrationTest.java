package org.atlasapi.remotesite.metabroadcast.similar;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.testing.BrandTestDataBuilder;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class SimilarContentIntegrationTest {

    private static final Set<String> GENRE_SET_A = ImmutableSet.of("http://genres.com/1", "http://genres.com/2");
    private static final Set<String> GENRE_SET_B = ImmutableSet.of("http://genres.com/2", "http://genres.com/3");
    
    private final SimilarContentModule similarContentModule = new SimilarContentModule();
    private final ContentLister contentLister = mock(ContentLister.class);
    
    private SimilarContentProvider similarContentProvider;
    
    @Before
    public void setUp() {
        similarContentModule.contentLister = contentLister;
        similarContentProvider = similarContentModule.similarContentProvider();
    }
    
    @Test
    public void testSimilarContentProvider() {
        ContentListingCriteria expectedCriteria = ContentListingCriteria
                .defaultCriteria()
                .forPublisher(Publisher.PA)
                .build();
        
        List<Content> testBrands = testBrands();
        when(contentLister.listContent(expectedCriteria)).thenReturn(testBrands.iterator());
        similarContentProvider.initialise();
        List<Long> similar = similarContentProvider.similarTo(testBrands.get(0));
        ImmutableList<Long> expectedIds = ImmutableList.copyOf(Iterables.transform(testBrands.subList(1, 11), Content.TO_ID));
        assertThat(similar, containsInAnyOrder(expectedIds.toArray()));
    }
    
    private List<Content> testBrands() {
        List<Content> brands = Lists.newArrayList();
        for (int i = 0; i < 50; i++) {
            Set<String> genres = i < 11 ? GENRE_SET_A : GENRE_SET_B;
            brands.add(testBrand(i, genres));
        }
        return brands;
    }
    private Brand testBrand(int id, Iterable<String> genres) {
        return BrandTestDataBuilder
                    .brand()
                    .withCanonicalUri(String.format("http://brand.com/%d", id))
                    .withGenres(genres)
                    .withId(id)
                    .build();
    }
}
