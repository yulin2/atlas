package org.atlasapi.remotesite.seesaw.selftest;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.seesaw.SeesawAtoZBrandsAdapter;

public class SeesawAtoZBrandsAdapterTest  extends TestCase{

    SiteSpecificAdapter<Playlist> adapter = new SeesawAtoZBrandsAdapter(HttpClients.webserviceClient());

    public void testShouldGetBrand() throws Exception {
        Playlist aToH = adapter.fetch("http://www.seesaw.com/AtoZ/A");
        
        assertNotNull(aToH);
        assertTrue(containsPlaylist("battlestar galactica", aToH));
        Playlist galactica = getPlaylist("battlestar galactica", aToH);
        assertTrue(galactica.getItems().size() > 0);
        assertTrue(galactica.getGenres().contains("http://www.seesaw.com/TV/Drama"));
        Item firstItem = galactica.getItems().get(0);
        assertTrue(firstItem instanceof Episode);
        Episode firstEpisode = (Episode) firstItem;
        assertEquals("33", firstEpisode.getTitle());
        assertTrue(firstEpisode.getDescription().equalsIgnoreCase("Galactica is on the run and the crew is put to the test as sleep deprivation mixes with the already gloomy reality of dealing with a Cylon-apocalypse. The ship has 33 minutes to make a Jump, elude the Cylons and survive another day."));
        assertEquals(Integer.valueOf(1), firstEpisode.getSeriesNumber());
        assertEquals(Integer.valueOf(1), firstEpisode.getEpisodeNumber());
        
        assertTrue(firstItem.getVersions().size() > 0);
        Version firstVersion = firstItem.getVersions().iterator().next();
        assertEquals(Integer.valueOf(42 * 60), firstVersion.getPublishedDuration());
        
        Playlist iToO = adapter.fetch("http://www.seesaw.com/AtoZ/I");
        Playlist pToZ = adapter.fetch("http://www.seesaw.com/AtoZ/P");
        Playlist zeroToNine = adapter.fetch("http://www.seesaw.com/AtoZ/0");
        
        
        assertNotNull(iToO);
        assertNotNull(pToZ);
        assertNotNull(zeroToNine);
        
        assertTrue(containsPlaylist("IPC World Swimming Championships", iToO));
        assertTrue(containsPlaylist("Jamie Oliver: Eat to Save Your Life", iToO));
        assertTrue(containsPlaylist("The Thick of It", pToZ));
    }
    
    private boolean containsPlaylist(String title, Playlist playlist) {
        return getPlaylist(title, playlist) != null;
    }
    
    private Playlist getPlaylist(String title, Playlist playlist) {
        for (Playlist brand : playlist.getPlaylists()) {
            if (brand.getTitle().equalsIgnoreCase(title)) {
                return brand;
            }
        }
        
        return null;
    }
    
}
