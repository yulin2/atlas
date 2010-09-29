package org.atlasapi.remotesite.seesaw;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.persistence.system.RemoteSiteClient;

public class SeesawSitemapClientTest extends TestCase {
    private final RemoteSiteClient<List<String>> client = new SeesawSitemapClient();

    public void testShouldRetrieveSitemapList() throws Exception {
        List<String> urls = client.get("http://www.seesaw.com/googlesitemaps/GoogleSitemap/2/0");
        assertTrue(urls.size() > 0);
    }
}
