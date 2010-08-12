package org.atlasapi.remotesite.hulu;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

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
