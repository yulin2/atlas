package org.atlasapi.remotesite.seesaw;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class SeesawItemAdapterTest extends TestCase {
    SiteSpecificAdapter<Episode> adapter = new SeesawItemAdapter(HttpClients.webserviceClient());
    
    public void testShouldGetProgram() {
        Episode afghanStar = adapter.fetch("http://www.seesaw.com/TV/Factual/p-1167-Afghan-Star");
        assertEquals("Afghan Star", afghanStar.getTitle());
        assertNotNull(afghanStar.getDescription());
    }
}
