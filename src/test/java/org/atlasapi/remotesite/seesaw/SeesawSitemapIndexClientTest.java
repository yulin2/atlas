package org.atlasapi.remotesite.seesaw;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.persistence.system.RemoteSiteClient;

public class SeesawSitemapIndexClientTest extends TestCase {
    private final RemoteSiteClient<List<String>> client = new SeesawSitemapIndexClient();

    public void testShouldRetrieveSitemapList() throws Exception {
        List<String> sitemaps = client.get(SeesawSitemapUpdater.SITEMAP_INDEX);
        assertTrue(sitemaps.size() > 0);
    }
}
