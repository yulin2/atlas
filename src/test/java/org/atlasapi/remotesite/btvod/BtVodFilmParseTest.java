package org.atlasapi.remotesite.btvod;

import static org.atlasapi.remotesite.btvod.BtVodEpisodeParseTest.getContentElementFromFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Set;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.intl.Countries;

public class BtVodFilmParseTest {

    private final BtVodContentCreator<Film> filmCreator = new BtVodFilmCreator();

    @Test
    public void testFilmParsing() throws ValidityException, ParsingException, IOException {
        BtVodItemDataExtractor dataExtractor = new BtVodItemDataExtractor();
        BtVodItemData data = dataExtractor.extract(getContentElementFromFile("btvod-film.xml"));
        
        Film film = filmCreator.extract(data);
        
        // check contents of film
        assertEquals("http://bt.com/titles/68541", film.getCanonicalUri());
        assertEquals("There's Something About Mary", film.getTitle());
        assertEquals("Now an adult, the high school nerd gets a chance to date the " +
        		"love of his life but faces competition from a shady private detective" +
        		" who sets out to discredit him.  Rating: 15", film.getDescription());
        assertThat(film.getYear(), is(1998));

        assertThat(film.getGenres().size(), is(1));
        assertEquals(ImmutableSet.of("http://bt.com/genres/romance"), film.getGenres());
        
        assertEquals(ImmutableSet.of("English"), film.getLanguages());
        
        assertEquals(ImmutableSet.of(new Certificate("15", Countries.GB)), film.getCertificates());
        
        assertEquals(ImmutableSet.of(
                "https://preproduction-movida.bebanjo.net/api/titles/68541",
                "FOX000025985"
            ), film.getAliases());
   
        Version version = Iterables.getOnlyElement(film.getVersions());
        assertThat(version.getDuration(), is(7140));
        assertThat(version.getPublishedDuration(), is(7140));
        
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        assertThat(encoding.getAvailableAt().size(), is(3));
        
        Set<Platform> platforms = Sets.newHashSet();
        
        for (Location location : encoding.getAvailableAt()) {
            assertEquals("http://bt.com/availability_windows/26445", location.getUri());
            assertEquals(new DateTime(2008, 12, 15, 0, 0, 0, 0).getMillis(), location.getPolicy().getAvailabilityStart().getMillis());
            assertEquals(new DateTime(2012, 12, 31, 23, 59, 59, 0).getMillis(), location.getPolicy().getAvailabilityEnd().getMillis());
            assertEquals(ImmutableSet.of(Countries.GB), location.getPolicy().getAvailableCountries());
            platforms.add(location.getPolicy().getPlatform());
        }
        
        assertEquals(ImmutableSet.of(Platform.BTVISION_CARDINAL, Platform.BTVISION_CLASSIC, Platform.YOUVIEW), platforms);
        
        assertEquals(Specialization.FILM, film.getSpecialization());
        
        assertEquals(Publisher.BT, film.getPublisher());
    }

}
