package org.atlasapi.remotesite.hulu;

import static org.atlasapi.remotesite.hulu.HuluItemAdapter.basicHuluItemAdapter;
import junit.framework.TestCase;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.hulu.HuluItemAdapter.HuluItemCanonicaliser;
import org.junit.Test;

import com.metabroadcast.common.intl.Countries;

public class HuluItemAdapterTest extends TestCase {
    
    private final AdapterLog log = new SystemOutAdapterLog();
    private final String episodeUrl = "http://www.hulu.com/watch/285014";
    private final String vanityUrl = episodeUrl+"/glee-asian-f#s-p1-so-i0";
    private final HuluItemAdapter adapter = basicHuluItemAdapter(new HttpBackedHuluClient(HttpClients.screenScrapingClient(),log));

    public void shouldRetrieveHuluItem() throws Exception {
        Episode item = (Episode) adapter.fetch(vanityUrl);
        assertNotNull(item);

        ParentRef brand = item.getContainer();
        assertNotNull(brand);
        assertEquals("http://www.hulu.com/glee", brand.getId());

        assertEquals("Asian F", item.getTitle());
        assertEquals(episodeUrl, item.getCanonicalUri());
        assertFalse(item.getTags().isEmpty());
        assertNotNull(item.getDescription());
        assertNotNull(item.getThumbnail());
        assertNotNull(item.getImage());
        assertEquals(Integer.valueOf(3), item.getSeriesNumber());
        assertEquals(Integer.valueOf(3), item.getEpisodeNumber());

        Version version = item.getVersions().iterator().next();
        assertNotNull(version);
        assertTrue(version.getDuration() > 0);

        Encoding encoding = version.getManifestedAs().iterator().next();
        assertNotNull(encoding);

        assertEquals(2, encoding.getAvailableAt().size());
        boolean foundLink = false;
        boolean foundEmbed = false;
        for (Location location: encoding.getAvailableAt()) {
            if (location.getTransportType() == TransportType.EMBED) {
                foundEmbed = true;
                assertEmbedLocation(location);
            } else {
                foundLink = true;
                assertLinkLocation(location);
            }
        }
        assertTrue(foundLink);
        assertTrue(foundEmbed);
    }
    
    private void assertEmbedLocation(Location location) {
        assertNotNull(location);
        if (location.getEmbedCode() != null) {
            assertNotNull(location.getEmbedCode());
        } else {
            assertNotNull(location.getUri());
        }
        assertEquals(TransportType.EMBED, location.getTransportType());
        assertEquals(true, location.getAvailable());
        assertEquals(Boolean.valueOf(true), location.getTransportIsLive());
        assertNotNull(location.getPolicy());
        assertFalse(location.getPolicy().getAvailableCountries().isEmpty());
        assertEquals(Countries.US, location.getPolicy().getAvailableCountries().iterator().next());
    }
    
    private void assertLinkLocation(Location location) {
        assertNotNull(location);
        assertNotNull(location.getUri()); 
        assertNull(location.getEmbedCode());
        assertEquals(TransportType.LINK, location.getTransportType());
        assertEquals(true, location.getAvailable());
        assertNotNull(location.getPolicy());
        assertFalse(location.getPolicy().getAvailableCountries().isEmpty());
        assertEquals(Countries.US, location.getPolicy().getAvailableCountries().iterator().next());
    }

    @Test
    public void testShouldBeAbleToFetch() throws Exception {
        assertTrue(adapter.canFetch("http://www.hulu.com/watch/152348/glee-funk"));
        assertTrue(adapter.canFetch("http://www.hulu.com/watch/152348"));
        assertFalse(adapter.canFetch("http://www.hulu.com/glee"));
    }

    @Test
    public void testCanonicaliser() throws Exception {
        assertEquals("http://www.hulu.com/watch/152348", new HuluItemCanonicaliser().canonicalise("http://www.hulu.com/watch/152348/glee-funk"));
        assertEquals("http://www.hulu.com/watch/152348", new HuluItemCanonicaliser().canonicalise("http://www.hulu.com/watch/152348"));
        assertNull(new HuluItemCanonicaliser().canonicalise("http://www.hulu.com/glee"));
    }
    
    @Test
    public void testShouldReturnNullOn404() { 
    	assertNull(adapter.fetch("http://www.hulu.com/watch/1782261234123421835349856794835649876439876"));
    }
}
