package org.uriplay.remotesite.hulu;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.HttpClients;
import org.uriplay.remotesite.SiteSpecificAdapter;

@SuppressWarnings("unchecked")
public class HuluAllBrandsAdapterTest extends MockObjectTestCase {
    SiteSpecificAdapter<Brand> brandAdapter = mock(SiteSpecificAdapter.class);
    HuluAllBrandsAdapter adapter = new HuluAllBrandsAdapter(HttpClients.webserviceClient(), brandAdapter);

    public void testShouldGetBrand() throws Exception {
        checking(new Expectations() {{
            allowing(brandAdapter).canFetch((String) with(anything())); will(returnValue(true));
            allowing(brandAdapter).fetch((String) with(anything())); will(returnValue(new Brand()));
        }});
        
        String uri = "http://www.hulu.com/browse/alphabetical/episodes";
        Playlist playlist = adapter.fetch(uri);
        
        assertNotNull(playlist);
        assertEquals(uri, playlist.getCanonicalUri());
        assertEquals("hulu:all_brands", playlist.getCurie());
        //assertFalse(playlist.getPlaylists().isEmpty());
    }
    
    public void testShouldBeAbleToFetchBrands() throws Exception {
        assertTrue(adapter.canFetch("http://www.hulu.com/browse/alphabetical/episodes"));
        assertFalse(adapter.canFetch("http://www.hulu.com/watch/123/glee"));
    }
}
