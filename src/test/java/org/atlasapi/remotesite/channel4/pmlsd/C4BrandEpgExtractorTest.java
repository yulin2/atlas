package org.atlasapi.remotesite.channel4.pmlsd;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi;
import org.atlasapi.remotesite.channel4.pmlsd.C4EpgEpisodeExtractor;
import org.junit.Test;

import com.google.common.io.Resources;
import com.metabroadcast.common.time.SystemClock;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

public class C4BrandEpgExtractorTest extends TestCase {
    
    private final AtomFeedBuilder gleeAtom = new AtomFeedBuilder(Resources.getResource(getClass(), "glee-epg.atom"));
    private final Feed gleeFeed = gleeAtom.build();
    
    private final C4EpgEpisodeExtractor extractor = new C4EpgEpisodeExtractor(new C4AtomApi(new C4DummyChannelResolver()), new SystemClock());
    
    @Test
    public void testShouldExtractBroadcast() throws Exception {

        Episode episode = extractor.extract((Entry)gleeFeed.getEntries().get(0));
        
        assertEquals("http://www.channel4.com/programmes/glee/episode-guide/series-1/episode-5", episode.getCanonicalUri());
        assertEquals(Integer.valueOf(1), episode.getSeriesNumber());
        assertEquals(Integer.valueOf(5), episode.getEpisodeNumber());
        
        assertFalse(episode.getVersions().isEmpty());
        assertEquals(1, episode.getVersions().size());
        
        Version version = episode.getVersions().iterator().next();
        assertFalse(version.getBroadcasts().isEmpty());
        assertEquals(1, version.getBroadcasts().size());
        
        Broadcast broadcast = version.getBroadcasts().iterator().next();
        
        assertTrue(broadcast.getAliasUrls().contains("tag:www.channel4.com,2009:slot/E439861"));
        assertEquals(Integer.valueOf(55*60), broadcast.getBroadcastDuration());
    }

}
