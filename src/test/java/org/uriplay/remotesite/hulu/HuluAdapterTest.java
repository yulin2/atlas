package org.uriplay.remotesite.hulu;

import junit.framework.TestCase;

import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Countries;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

public class HuluAdapterTest extends TestCase {
    HuluAdapter adapter = new HuluAdapter();
    
    public void testShouldRetrieveHuluItem() throws Exception {
        Episode item = (Episode) adapter.fetch("http://www.hulu.com/watch/152348/glee-funk", null);
        assertNotNull(item);
        
        Brand brand = item.getBrand();
        assertNotNull(brand);
        assertEquals("Glee", brand.getTitle());
        assertEquals("http://www.hulu.com/glee", brand.getCanonicalUri());
        assertNotNull(brand.getDescription());
        
        assertEquals("Funk", item.getTitle());
        assertEquals("http://www.hulu.com/watch/152348", item.getCanonicalUri());
        assertFalse(item.getTags().isEmpty());
        assertNotNull(item.getDescription());
        assertNotNull(item.getThumbnail());
        assertNotNull(item.getImage());
        assertEquals(Integer.valueOf(1), item.getSeriesNumber());
        assertEquals(Integer.valueOf(21), item.getEpisodeNumber());
        
        Version version = item.getVersions().iterator().next();
        assertNotNull(version);
        assertTrue(version.getDuration() > 0);
        
        Encoding encoding = version.getManifestedAs().iterator().next();
        assertNotNull(encoding);
        
        Location location = encoding.getAvailableAt().iterator().next();
        assertNotNull(location);
        assertNotNull(location.getEmbedCode());
        assertEquals(TransportType.EMBED, location.getTransportType());
        assertEquals(true, location.getAvailable());
        assertEquals(Boolean.valueOf(true), location.getTransportIsLive());
        assertNotNull(location.getPolicy());
        assertFalse(location.getPolicy().getAvailableCountries().isEmpty());
        assertEquals(Countries.US, location.getPolicy().getAvailableCountries().iterator().next());
    }
}
