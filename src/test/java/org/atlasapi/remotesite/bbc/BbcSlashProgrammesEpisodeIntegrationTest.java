package org.atlasapi.remotesite.bbc;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;

public class BbcSlashProgrammesEpisodeIntegrationTest extends TestCase {

    private BbcProgrammeAdapter adapter = new BbcProgrammeAdapter(new SystemOutAdapterLog());
    
    public void testClientGetsEpisode() throws Exception {
        Content programme = adapter.fetch("http://www.bbc.co.uk/programmes/b00w4hjy");
        assertNotNull(programme);
        
        assertNotNull(programme.getClips());
        assertFalse(programme.getClips().isEmpty());
        
        for (Clip clip: programme.getClips()) {
            assertNotNull(clip.getCanonicalUri());
            assertNotNull(clip.getVersions());
            assertFalse(clip.getVersions().isEmpty());
        }
    }
}
