package org.atlasapi.remotesite.channel4;

import java.util.List;

import junit.framework.TestCase;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.NullAdapterLog;

import com.google.common.io.Resources;

public class C4ClipExtractorTest extends TestCase {

	private final AtomFeedBuilder fourOdFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "hestons-mission-impossible.atom"));
	
	public void testExtractingClips() throws Exception {
		
		List<Clip> clips = new C4EpisodesExtractor(null, new NullAdapterLog()).includeOnDemands().extractClips(fourOdFeed.build());
		assertEquals(5, clips.size());
		
		for (Clip clip: clips) {
		    assertNotNull(clip.getCanonicalUri());
		    assertNotNull(clip.getTitle());
		    assertEquals(MediaType.VIDEO, clip.getMediaType());
		    assertFalse(clip.getIsLongForm());
		    
		    assertFalse(clip.getVersions().isEmpty());
		    for (Version version: clip.getVersions()) {
		        assertFalse(version.getManifestedAs().isEmpty());
		        for (Encoding encoding: version.getManifestedAs()) {
		            assertEquals(2, encoding.getAvailableAt().size());
		            
		            Location link = locationOfType(encoding.getAvailableAt(), TransportType.LINK);
		            assertNotNull(link);
		            assertNotNull(link.getUri());
		            
		            Location embed = locationOfType(encoding.getAvailableAt(), TransportType.EMBED);
		            assertNotNull(embed);
                    assertNull(embed.getUri());
                    assertNotNull(embed.getEmbedId());
                    assertTrue(embed.getEmbedCode().contains(embed.getEmbedId()));
                    assertEquals(TransportSubType.BRIGHTCOVE, embed.getTransportSubType());
		        }
		    }
		}
	}
	
	private Location locationOfType(Iterable<Location> locations, TransportType type) {
	    for (Location location: locations) {
	        if (type.equals(location.getTransportType())) {
	            return location;
	        }
	    }
	    return null;
	}
}
