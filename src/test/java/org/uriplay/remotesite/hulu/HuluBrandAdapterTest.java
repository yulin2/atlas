package org.uriplay.remotesite.hulu;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Episode;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.hulu.HuluBrandAdapter.HuluBrandCanonicaliser;

import com.metabroadcast.common.http.SimpleHttpClientBuilder;

@SuppressWarnings("unchecked")
public class HuluBrandAdapterTest extends MockObjectTestCase {
    SiteSpecificAdapter<Episode> episodeAdapter = mock(SiteSpecificAdapter.class);
    HuluBrandContentExtractor brandExtractor = new HuluBrandContentExtractor();
    HuluBrandAdapter adapter = new HuluBrandAdapter(new SimpleHttpClientBuilder().build(), brandExtractor);

    public void testShouldGetBrand() throws Exception {
        checking(new Expectations() {{
            allowing(episodeAdapter).fetch((String) with(anything()), (RequestTimer) with(anything())); will(returnValue(new Episode()));
        }});
        adapter.setEpisodeAdapter(episodeAdapter);
        
        String uri = "http://www.hulu.com/glee";
        Brand brand = adapter.fetch(uri, null);
        
        assertNotNull(brand);
        assertEquals("Glee", brand.getTitle());
        assertNotNull(brand.getDescription());
        assertEquals(uri, brand.getCanonicalUri());
        assertEquals("hulu:glee", brand.getCurie());
        assertNotNull(brand.getImage());
        assertNotNull(brand.getThumbnail());
        assertFalse(brand.getTags().isEmpty());
        assertFalse(brand.getItems().isEmpty());
    }
    
    public void testShouldBeAbleToFetchBrands() throws Exception {
        assertTrue(adapter.canFetch("http://www.hulu.com/glee"));
        assertFalse(adapter.canFetch("http://www.hulu.com/watch/123/glee"));
    }
    
    public void testShouldCanonicalise() throws Exception {
        assertEquals("http://www.hulu.com/glee", new HuluBrandCanonicaliser().canonicalise("http://www.hulu.com/glee"));
        assertEquals("http://www.hulu.com/americas-game", new HuluBrandCanonicaliser().canonicalise("http://www.hulu.com/nfl/americas-game"));
        assertNull(new HuluBrandCanonicaliser().canonicalise("http://www.hulu.com/watch/489427/glee"));
    }
}
