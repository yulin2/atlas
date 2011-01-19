package org.atlasapi.remotesite.youtube.user;

import junit.framework.TestCase;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;

public class YouTubeUserIntegrationTest extends TestCase {

    private YouTubeUserAdapter adapter = new YouTubeUserAdapter();
    
    public void testShouldRetrievePlaylist() throws Exception {
        String uri = "http://www.youtube.com/user/YTdebates";
        assertTrue(adapter.canFetch(uri));
        
        ContentGroup userPlaylist = adapter.fetch(uri);
        assertNotNull(userPlaylist);
        assertEquals(new YouTubeUserCanonicaliser().canonicalise(uri), userPlaylist.getCanonicalUri());
        assertEquals(YouTubeUserCanonicaliser.curieFor(uri), userPlaylist.getCurie());
        assertEquals(Publisher.YOUTUBE, userPlaylist.getPublisher());
        
        assertFalse(userPlaylist.getContents().isEmpty());
    }
}
