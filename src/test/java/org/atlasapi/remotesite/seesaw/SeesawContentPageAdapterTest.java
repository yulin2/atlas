package org.atlasapi.remotesite.seesaw;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class SeesawContentPageAdapterTest extends TestCase {
    SiteSpecificAdapter<Brand> adapter = new SeesawContentPageAdapter(HttpClients.webserviceClient());
    
    public void testShouldGetBrand() {
        Brand galactica = adapter.fetch("http://www.seesaw.com/TV/Drama/b-28373-Battlestar-Galactica");
        assertEquals("Battlestar Galactica", galactica.getTitle());
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
    }
    
    public void testShouldGetSeries() {
        Brand thirtyRock = adapter.fetch("http://www.seesaw.com/TV/Comedy/s-29158-30-Rock");
        assertEquals("30 Rock", thirtyRock.getTitle());
        assertTrue(thirtyRock.getItems().size() > 0);
        assertTrue(thirtyRock.getGenres().contains("http://www.seesaw.com/TV/Comedy"));
        Item firstItem = thirtyRock.getItems().get(0);
        assertTrue(firstItem instanceof Episode);
        Episode firstEpisode = (Episode) firstItem;
        assertEquals("Pilot", firstEpisode.getTitle());
    }
    
    public void testShouldGetProgram() {
        Brand afghanStar = adapter.fetch("http://www.seesaw.com/TV/Factual/p-1167-Afghan-Star");
        assertEquals("Afghan Star", afghanStar.getTitle());
        assertEquals(1, afghanStar.getItems().size());
    }
    
    public void testShouldBeAbleToFetch() {
        assertTrue(adapter.canFetch("http://www.seesaw.com/TV/Factual/p-1167-Afghan-Star"));
        assertFalse(adapter.canFetch("http://www.seesaw.com/AtoZ/A"));
    }
}
