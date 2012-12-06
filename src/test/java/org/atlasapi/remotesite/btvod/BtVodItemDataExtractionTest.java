package org.atlasapi.remotesite.btvod;

import static org.atlasapi.remotesite.btvod.BtVodEpisodeParseTest.getContentElementFromFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.Iterables;

public class BtVodItemDataExtractionTest {

    @Test
    public void testItemDataExtraction() throws ValidityException, ParsingException, IOException {

            BtVodItemDataExtractor dataExtractor = new BtVodItemDataExtractor();
            BtVodItemData data = dataExtractor.extract(getContentElementFromFile("btvod-episode.xml"));
            
            assertEquals("http://bt.com/titles/76469", data.getUri());
            assertEquals("Episode 3", data.getTitle());
            assertEquals("Gavin and Stacey tell their friends and family about their surprise engagement. " +
            		"BBC Hits is a commercial service. Programmes made available here were previously " +
            		"available for free on BBC channels.", data.getDescription());
            assertThat(data.getYear(), is(2007));

            assertThat(data.getGenres().size(), is(3));
            assertEquals(ImmutableSet.of(
                    "http://bt.com/genres/comedy", 
                    "http://bt.com/genres/uk", 
                    "http://bt.com/genres/00s"
                ), data.getGenres());
            
            assertEquals("English", data.getLanguage());
            
            assertEquals("15", data.getCertificate());
            
            assertEquals("https://preproduction-movida.bebanjo.net/api/titles/76469", data.getSelfLink());
            assertEquals("BBW000067503", data.getExternalId());
       
            BtVodLocationData location = Iterables.getOnlyElement(data.getLocations());
            assertThat(location.getDuration(), is(1800));
            
            assertEquals("http://bt.com/availability_windows/34373", location.getUri());
            assertEquals(new DateTime(2010, 1, 1, 0, 0, 0, 0).getMillis(), location.getAvailabilityStart().getMillis());
            assertEquals(new DateTime(2013, 2, 1, 23, 59, 59, 0).getMillis(), location.getAvailabilityEnd().getMillis());
            
            assertEquals(ImmutableSet.of(Platform.BTVISION_CARDINAL, Platform.BTVISION_CLASSIC, Platform.YOUVIEW), location.getPlatforms());
            
//            assertEquals(episode.getSeriesRef(), new ParentRef(""));
            assertTrue(data.getContainer().isPresent());
            assertTrue(data.getContainerTitle().isPresent());
            assertTrue(data.getContainerSelfLink().isPresent());
            assertTrue(data.getContainerExternalId().isPresent());
            assertEquals("http://bt.com/title_groups/11225", data.getContainer().get());
            assertEquals("Gavin and Stacey: S01", data.getContainerTitle().get());
            assertEquals("https://preproduction-movida.bebanjo.net/api/title_groups/11225", data.getContainerSelfLink().get());
            assertEquals("BBW_Gavin and Stacey_s1", data.getContainerExternalId().get());

            assertTrue(data.getSeriesNumber().isPresent());
            assertTrue(data.getEpisodeNumber().isPresent());
            assertThat(data.getSeriesNumber().get(), is(1));
            assertThat(data.getEpisodeNumber().get(), is(3));
    }
    
    
    @Test
    public void testDataExtractionForFilm() throws ValidityException, ParsingException, IOException {
        BtVodItemDataExtractor dataExtractor = new BtVodItemDataExtractor();
        BtVodItemData data = dataExtractor.extract(getContentElementFromFile("btvod-film.xml"));
        
        // check contents of data object
        assertEquals("http://bt.com/titles/68541", data.getUri());
        assertEquals("There's Something About Mary", data.getTitle());
        assertEquals("Now an adult, the high school nerd gets a chance to date the " +
                "love of his life but faces competition from a shady private detective" +
                " who sets out to discredit him.  Rating: 15", data.getDescription());
        assertThat(data.getYear(), is(1998));

        assertThat(data.getGenres().size(), is(1));
        assertEquals(ImmutableSet.of("http://bt.com/genres/romance"), data.getGenres());
        
        assertEquals("English", data.getLanguage());
        
        assertEquals("15", data.getCertificate());
        
        assertEquals("https://preproduction-movida.bebanjo.net/api/titles/68541", data.getSelfLink());
                assertEquals("FOX000025985", data.getExternalId());
   
        BtVodLocationData location = Iterables.getOnlyElement(data.getLocations());
        assertThat(location.getDuration(), is(7140));
        
        assertEquals("http://bt.com/availability_windows/26445", location.getUri());
        assertEquals(new DateTime(2008, 12, 15, 0, 0, 0, 0).getMillis(), location.getAvailabilityStart().getMillis());
        assertEquals(new DateTime(2012, 12, 31, 23, 59, 59, 0).getMillis(), location.getAvailabilityEnd().getMillis());
        
        assertEquals(ImmutableSet.of(Platform.BTVISION_CARDINAL, Platform.BTVISION_CLASSIC, Platform.YOUVIEW), location.getPlatforms());
        
        assertFalse(data.getContainer().isPresent());
        assertFalse(data.getContainerTitle().isPresent());
        assertFalse(data.getContainerSelfLink().isPresent());
        assertFalse(data.getContainerExternalId().isPresent());
        assertFalse(data.getSeriesNumber().isPresent());
        assertFalse(data.getEpisodeNumber().isPresent());
    }

}
