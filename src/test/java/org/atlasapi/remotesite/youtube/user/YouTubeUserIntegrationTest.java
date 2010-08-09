package org.atlasapi.remotesite.youtube.user;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.youtube.user.YouTubeUserAdapter;
import org.atlasapi.remotesite.youtube.user.YouTubeUserCanonicaliser;

public class YouTubeUserIntegrationTest extends TestCase {

    private YouTubeUserAdapter adapter = new YouTubeUserAdapter();
    
    public void testShouldRetrievePlaylist() throws Exception {
        String uri = "http://gdata.youtube.com/feeds/api/users/YTdebates/playlists";
        assertTrue(adapter.canFetch(uri));
        
        Playlist userPlaylist = adapter.fetch(uri);
        assertNotNull(userPlaylist);
        assertEquals(new YouTubeUserCanonicaliser().canonicalise(uri), userPlaylist.getCanonicalUri());
        assertEquals(YouTubeUserCanonicaliser.curieFor(uri), userPlaylist.getCurie());
        assertEquals(Publisher.YOUTUBE, userPlaylist.getPublisher());
        
        assertFalse(userPlaylist.getPlaylists().isEmpty());
        assertTrue(userPlaylist.getItems().isEmpty());
    }
}
