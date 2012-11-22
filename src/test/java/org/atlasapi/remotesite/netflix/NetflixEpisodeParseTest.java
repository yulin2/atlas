package org.atlasapi.remotesite.netflix;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Version;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.intl.Countries;

public class NetflixEpisodeParseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testBrandParsing() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-episode.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixContentExtractor<Episode> episodeExtractor = new NetflixEpisodeExtractor();        
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class), episodeExtractor, Mockito.mock(NetflixContentExtractor.class));

        Set<Content> contents = Sets.newHashSet();
        for (int i = 0; i < rootElement.getChildElements().size(); i++) {
            contents = Sets.union(contents, extractor.extract(rootElement.getChildElements().get(i)));
        }
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(2));
        
        // check that it is a Episode
        Content content = contents.iterator().next();
        assertTrue(content instanceof Episode);
        Episode episode = (Episode) content;
        
        // check contents of item
        assertThat(episode.getCanonicalUri(), equalTo("http://gb.netflix.com/episodes/70151113"));
        assertThat(episode.getTitle(), equalTo("Orientation"));
        assertThat(episode.getDescription(), equalTo("Recent events have all the heroes looking for answers. " +
        		"Claire tries to start over at college, but she hits a snag."));
        assertThat(episode.getYear(), equalTo(2006));

        assertThat(episode.getGenres().size(), is(6));
        assertEquals(episode.getGenres(), ImmutableSet.of(
                "http://gb.netflix.com/genres/tvshows", 
                "http://gb.netflix.com/genres/ustvshows", 
                "http://gb.netflix.com/genres/tvaction&adventure",
                "http://gb.netflix.com/genres/tvdramas",
                "http://gb.netflix.com/genres/tvsci-fi&fantasy",
                "http://gb.netflix.com/genres/ustvdramas"
            ));
        
        assertThat(episode.getCertificates().size(), is(1));
        for (Certificate cert : episode.getCertificates()) {
            assertThat(cert.classification(), equalTo("15"));
            assertThat(cert.country(), equalTo(Countries.GB));
        }
        
        assertThat(episode.getAliases().size(), is(1));
        for (String alias : episode.getAliases()) {
            assertThat(alias, equalTo("http://api.netflix.com/catalog/titles/programs/262101/70151113"));
        }

        assertThat(episode.getVersions().size(), is(1));
        for (Version version : episode.getVersions()) {
            assertThat(version.getDuration(), equalTo(2608));
        }
        
        // TODO Container and SeriesSummary

        assertThat(episode.getSeriesNumber(), is(4));
        assertThat(episode.getEpisodeNumber(), is(1));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testBrandParsingNoLongSynopsis() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-episode-short-synopsis.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixContentExtractor<Episode> episodeExtractor = new NetflixEpisodeExtractor();        
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class), episodeExtractor, Mockito.mock(NetflixContentExtractor.class));

        Set<Content> contents = Sets.newHashSet();
        for (int i = 0; i < rootElement.getChildElements().size(); i++) {
            contents = Sets.union(contents, extractor.extract(rootElement.getChildElements().get(i)));
        }
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(2));
        
        // check that it is a Episode
        Content content = contents.iterator().next();
        assertTrue(content instanceof Episode);
        Episode episode = (Episode) content;
        
        // check contents of item
        assertThat(episode.getDescription(), equalTo("Recent events have all the heroes looking for answers. " +
                "Claire tries to start over at college, but she hits a snag."));
    }
}
