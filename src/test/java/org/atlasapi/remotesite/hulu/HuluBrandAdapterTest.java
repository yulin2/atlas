package org.atlasapi.remotesite.hulu;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.hulu.HuluBrandAdapter.HuluBrandCanonicaliser;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

@SuppressWarnings("unchecked")
public class HuluBrandAdapterTest extends MockObjectTestCase {
    SiteSpecificAdapter<Episode> episodeAdapter = mock(SiteSpecificAdapter.class);
    HuluBrandContentExtractor brandExtractor = new HuluBrandContentExtractor();
    HuluBrandAdapter adapter = new HuluBrandAdapter(HttpClients.webserviceClient(), brandExtractor);

    public void testShouldGetBrand() throws Exception {
        checking(new Expectations() {{
            allowing(episodeAdapter).fetch((String) with(anything())); will(returnValue(new Episode()));
        }});
        adapter.setEpisodeAdapter(episodeAdapter);
        
        String uri = "http://www.hulu.com/glee";
        Brand brand = adapter.fetch(uri);
        
        assertNotNull(brand);
        assertEquals("Glee", brand.getTitle());
        assertNotNull(brand.getDescription());
        assertEquals(uri, brand.getCanonicalUri());
        assertEquals("hulu:glee", brand.getCurie());
        assertNotNull(brand.getImage());
        assertNotNull(brand.getThumbnail());
        assertFalse(brand.getTags().isEmpty());
        assertFalse(brand.getContents().isEmpty());
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
