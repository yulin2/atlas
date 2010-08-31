package org.atlasapi.remotesite.tvblob;

import java.io.InputStream;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.common.io.Resources;

public class TVBlobDayExtractorTest extends TestCase {

    private ContentExtractor<InputStream, Playlist> extractor = new TVBlobDayExtractor("raiuno");
    
    public void testShouldRetrievePlaylistAndItems() throws Exception {
        InputStream is = Resources.getResource(getClass(), "today.json").openStream();
        
        Playlist playlist = extractor.extract(is);
        assertNotNull(playlist);
        assertFalse(playlist.getItems().isEmpty());
        
        boolean foundMoreThanOne = false;
        for (Item item: playlist.getItems()) {
            Episode episode = (Episode) item;
            if (episode.getBrand() != null) {
                assertNotNull(episode.getBrand().getCanonicalUri());
            }
            assertNotNull(episode.getCanonicalUri());
            assertFalse(episode.getVersions().isEmpty());
            Version version = episode.getVersions().iterator().next();
            if (version.getBroadcasts().size() > 1) {
                foundMoreThanOne = true;
                assertEquals(2, version.getBroadcasts().size());
            }
            
            for (Broadcast broadcast: version.getBroadcasts()) {
                assertEquals("http://tvblob.com/channel/raiuno", broadcast.getBroadcastOn());
                assertNotNull(broadcast.getTransmissionTime());
            }
        }
        
        assertTrue(foundMoreThanOne);
    }
}
