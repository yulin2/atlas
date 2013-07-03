package org.atlasapi.remotesite.youtube;

import junit.framework.TestCase;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;

public class YouTubeFeedIntegrationTest extends TestCase {

    private YouTubeFeedAdapter feedAdapter = new YouTubeFeedAdapter();

    public void testShouldRetrievePlaylist() throws Exception {
        String uri = "http://gdata.youtube.com/feeds/api/standardfeeds/top_rated";
        assertTrue(feedAdapter.canFetch(uri));

        ContentGroup playlist = feedAdapter.fetch(uri);
        assertNotNull(playlist);
        assertEquals(new YouTubeFeedCanonicaliser().canonicalise(uri),
                playlist.getCanonicalUri());
        assertEquals(YouTubeFeedCanonicaliser.curieFor(uri),
                playlist.getCurie());
        assertEquals(Publisher.YOUTUBE, playlist.getPublisher());

        assertFalse(playlist.getContents().isEmpty());
    }
}
