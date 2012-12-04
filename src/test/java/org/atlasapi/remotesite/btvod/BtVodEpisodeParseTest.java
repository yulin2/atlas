package org.atlasapi.remotesite.btvod;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableSet;
import com.google.inject.internal.Iterables;
import com.google.inject.internal.Sets;
import com.metabroadcast.common.intl.Countries;

public class BtVodEpisodeParseTest {
    
    private final BtVodContentCreator<Episode> episodeCreator = new BtVodEpisodeCreator();

    @Test
    public void testEpisodeParsing() throws ValidityException, ParsingException, IOException {
        BtVodItemDataExtractor dataExtractor = new BtVodItemDataExtractor();
        BtVodItemData data = dataExtractor.extract(getContentElementFromFile("btvod-episode.xml"));
        
        Episode episode = episodeCreator.extract(data);
        
        // check contents of item
        assertEquals("http://bt.com/titles/76469", episode.getCanonicalUri());
        assertEquals("Episode 3", episode.getTitle());
        assertEquals("Gavin and Stacey tell their friends and family about their surprise" +
                " engagement. BBC Hits is a commercial service. Programmes made available here were previously available " +
                "for free on BBC channels.", episode.getDescription());
        assertThat(episode.getYear(), is(2007));

        assertThat(episode.getGenres().size(), is(3));
        assertEquals(ImmutableSet.of(
                "http://bt.com/genres/comedy", 
                "http://bt.com/genres/uk", 
                "http://bt.com/genres/00s"
            ), episode.getGenres());
        
        assertEquals(ImmutableSet.of("English"), episode.getLanguages());
        
        assertEquals(ImmutableSet.of(new Certificate("15", Countries.GB)), episode.getCertificates());
        
        assertEquals(ImmutableSet.of(
                "https://preproduction-movida.bebanjo.net/api/titles/76469",
                "BBW000067503"
            ), episode.getAliases());
   
        Version version = Iterables.getOnlyElement(episode.getVersions());
        assertThat(version.getDuration(), is(1800));
        assertThat(version.getPublishedDuration(), is(1800));
        
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        assertThat(encoding.getAvailableAt().size(), is(3));
        
        Set<Platform> platforms = Sets.newHashSet();
        
        for (Location location : encoding.getAvailableAt()) {
            assertEquals("http://bt.com/availability_windows/34373", location.getUri());
            assertEquals(new DateTime(2010, 1, 1, 0, 0, 0, 0).getMillis(), location.getPolicy().getAvailabilityStart().getMillis());
            assertEquals(new DateTime(2013, 2, 1, 23, 59, 59, 0).getMillis(), location.getPolicy().getAvailabilityEnd().getMillis());
            assertEquals(ImmutableSet.of(Countries.GB), location.getPolicy().getAvailableCountries());
            platforms.add(location.getPolicy().getPlatform());
        }
        
        assertEquals(ImmutableSet.of(Platform.BTVISION_CARDINAL, Platform.BTVISION_CLASSIC, Platform.YOUVIEW), platforms);
        
        assertEquals(new ParentRef("http://bt.com/title_groups/11225"), episode.getSeriesRef());
        assertEquals(new ParentRef("http://bt.com/title_groups/11225"), episode.getContainer());

        assertThat(episode.getSeriesNumber(), is(1));
        assertThat(episode.getEpisodeNumber(), is(3));

        assertEquals(Specialization.TV, episode.getSpecialization());
        
        assertEquals(Publisher.BT, episode.getPublisher());
    }
    
    public static Element getContentElementFromFile(String fileName) throws ValidityException, ParsingException, IOException {
        Document btVodData;
        btVodData = new Builder().build(new ClassPathResource(fileName).getInputStream());
        return btVodData.getRootElement();
    }

}
