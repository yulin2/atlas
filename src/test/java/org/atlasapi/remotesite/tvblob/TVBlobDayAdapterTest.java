package org.atlasapi.remotesite.tvblob;

import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.google.inject.internal.Sets;
import com.metabroadcast.common.time.DateTimeZones;

public class TVBlobDayAdapterTest extends TestCase {
    private SiteSpecificAdapter<Playlist> adapter = new TVBlobDayAdapter();

    public void ignoreShouldRetrieveToday() throws Exception {
        DateTime begin = new DateTime();
        Playlist playlist = adapter.fetch("http://epgadmin.tvblob.com/api/raiuno/programmes/schedules/tomorrow.json");
        DateTime end = new DateTime();

        assertEquals("tvblob:playlist_raiuno_" + new DateTime(DateTimeZones.UTC).toString("yyyyMMdd"), playlist
                        .getCurie());
        assertFalse(playlist.getItems().isEmpty());

        Set<String> brandUris = Sets.newHashSet();

        int maxBroadcasts = 0;
        int totalBroadcasts = 0;
        for (Item item : playlist.getItems()) {
            assertTrue(item instanceof Episode);
            Episode episode = (Episode) item;
            if (episode.getBrand() != null) {
                brandUris.add(episode.getBrand().getCanonicalUri());
            }

            Version version = item.getVersions().iterator().next();
            int numBroadcasts = version.getBroadcasts().size();
            if (numBroadcasts > maxBroadcasts) {
                maxBroadcasts = numBroadcasts;
            }
            totalBroadcasts += numBroadcasts;
        }

        Period period = new Period(begin, end);
        System.out.println("Generated the playlist in " + period.getMinutes() + " minutes and " + period.getSeconds()
                        + " seconds with " + playlist.getItems().size()
                        + " episodes, with a max number of broadcasts of " + maxBroadcasts + " and " + totalBroadcasts
                        + " overall. Total of "+brandUris.size()+" brands");
    }
    
    public void testCanFetch() {
        assertTrue(adapter.canFetch("http://epgadmin.tvblob.com/api/raiuno/programmes/schedules/tomorrow.json"));
    }
}
