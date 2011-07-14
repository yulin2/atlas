package org.atlasapi.remotesite.bbc;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.remotesite.channel4.RecordingContentWriter;

public class BbcSlashProgrammesEpisodeIntegrationTest extends TestCase {

    public void testClientGetsEpisode() throws Exception {
        
        RecordingContentWriter writer = new RecordingContentWriter();
        
        BbcProgrammeAdapter adapter = new BbcProgrammeAdapter(writer, new SystemOutAdapterLog());
        
        Content programme = (Content) adapter.createOrUpdate("http://www.bbc.co.uk/programmes/b011f3pk");
        assertNotNull(programme);
        
        assertNotNull(programme.getClips());
        assertFalse(programme.getClips().isEmpty());
        assertTrue(programme.getImage().contains("b00w4hjy"));
        assertTrue(programme.getThumbnail().contains("b00w4hjy"));
        
        for (Clip clip: programme.getClips()) {
            assertNotNull(clip.getCanonicalUri());
            assertNotNull(clip.getVersions());
            assertFalse(clip.getVersions().isEmpty());
        }
    }
}
