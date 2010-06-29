package org.uriplay.remotesite.hulu;

import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.SiteSpecificAdapter;

@SuppressWarnings("unchecked")
public class HuluRssAdapterTest extends MockObjectTestCase {
    SiteSpecificAdapter<Item> itemAdapter = mock(SiteSpecificAdapter.class);
    HuluRssAdapter adapter = new HuluRssAdapter();
    String feedUrl = "http://www.hulu.com/feed/recent/videos";
    
    public void ignoreShouldRetrieveRssPlaylsit() throws Exception {
        checking(new Expectations() {{
            allowing(itemAdapter).fetch((String) with(anything())); will(returnValue(new Item()));
        }});
        
        Playlist playlist = adapter.fetch(feedUrl);
        
        assertNotNull(playlist);
        assertNotNull(playlist.getTitle());
        assertNotNull(playlist.getDescription());
        assertEquals("hulu:recent_videos", playlist.getCurie());
        assertFalse(playlist.getItems().isEmpty());
        assertEquals(feedUrl, playlist.getCanonicalUri());
    }
    
    public void testShouldBeAbleToFetchFeeds() {
        assertTrue(adapter.canFetch(feedUrl));
    }
}
