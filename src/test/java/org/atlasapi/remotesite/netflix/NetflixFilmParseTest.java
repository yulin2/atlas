package org.atlasapi.remotesite.netflix;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.intl.Countries;


public class NetflixFilmParseTest {

    @Test
    public void testFilmParsing() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-film.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixFilmExtractor filmExtractor = new NetflixFilmExtractor();        
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(filmExtractor, Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class));

        Set<Content> contents = Sets.newHashSet();
        for (int i = 0; i < rootElement.getChildElements().size(); i++) {
            contents = Sets.union(contents, extractor.extract(rootElement.getChildElements().get(i)));
        }
        // check that a piece of content is returned
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(1));
        
        // check that it is a Film
        Content content = contents.iterator().next();
        assertTrue(content instanceof Film);
        Film film = (Film) content;
        
        // check contents of item
        assertThat(film.getCanonicalUri(), equalTo("http://gb.netflix.com/movies/21930861"));
        assertThat(film.getTitle(), equalTo("The Night Porter"));
        assertThat(film.getDescription(), equalTo("Twelve years after World War II, an ex-Nazi " +
        		"concentration camp guard working as a porter at a hotel is reunited with one of " +
        		"his victims, and contrary to expectations, they resume a twisted and doomed " +
        		"relationship."));
        assertThat(film.getYear(), equalTo(1974));
        
        // TODO add these in once people and genre matching is done
        //assertThat(film.getGenres(), equalTo(""));
        //assertThat(film.getPeople(), equalTo(""));
        
        assertThat(film.getCertificates().size(), is(1));
        for (Certificate cert : film.getCertificates()) {
            assertThat(cert.classification(), equalTo("18"));
            assertThat(cert.country(), equalTo(Countries.GB));
        }
        
        assertThat(film.getAliases().size(), is(1));
        for (String alias : film.getAliases()) {
            assertThat(alias, equalTo("http://api.netflix.com/catalog/titles/movies/21930861"));
        }

        assertThat(film.getVersions().size(), is(1));
        for (Version version : film.getVersions()) {
            assertThat(version.getDuration(), equalTo(7039));
        }
    }
    
    @Test
    public void testFilmParsingNoLongSynopsis() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-film-short-synopsis.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixFilmExtractor filmExtractor = new NetflixFilmExtractor();        
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(filmExtractor, Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class));


        Set<Content> contents = Sets.newHashSet();
        for (int i = 0; i < rootElement.getChildElements().size(); i++) {
            contents = Sets.union(contents, extractor.extract(rootElement.getChildElements().get(i)));
        }
        // check that a piece of content is returned
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(1));
        
        // check that it is a Film
        Content content = contents.iterator().next();
        assertTrue(content instanceof Film);
        Film film = (Film) content;
        
        // check contents of item
        assertThat(film.getDescription(), equalTo("Twelve years after World War II, an ex-Nazi " +
        		"concentration camp guard is reunited with one of his victims, and they resume a " +
        		"twisted relationship."));
    }
}
