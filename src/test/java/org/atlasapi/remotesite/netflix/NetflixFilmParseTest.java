package org.atlasapi.remotesite.netflix;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Version;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.intl.Countries;


public class NetflixFilmParseTest {

    @SuppressWarnings("unchecked")
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
        
        NetflixContentExtractor<Film> filmExtractor = new NetflixFilmExtractor();        
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
        
        assertThat(film.getGenres().size(), is(3));
        assertEquals(film.getGenres(), ImmutableSet.of("http://gb.netflix.com/genres/dramas", "http://gb.netflix.com/genres/classicdramas", "http://gb.netflix.com/genres/italianmovies"));
        
        CrewMember dirk = new CrewMember();
        dirk.withName("Dirk Bogarde").withRole(Role.ACTOR);
        dirk.setCanonicalUri("http://gb.netflix.com/people/9321");
        CrewMember charlotte = new CrewMember();
        charlotte.withName("Charlotte Rampling").withRole(Role.ACTOR);
        charlotte.setCanonicalUri("http://gb.netflix.com/people/76167");
        CrewMember philippe = new CrewMember();
        philippe.withName("Philippe Leroy").withRole(Role.ACTOR);
        philippe.setCanonicalUri("http://gb.netflix.com/people/20003720");
        CrewMember gabriele = new CrewMember();
        gabriele.withName("Gabriele Ferzetti").withRole(Role.ACTOR);
        gabriele.setCanonicalUri("http://gb.netflix.com/people/20014925");
        CrewMember giuseppe = new CrewMember();
        giuseppe.withName("Giuseppe Addobbati").withRole(Role.ACTOR);
        giuseppe.setCanonicalUri("http://gb.netflix.com/people/20040845");
        CrewMember isa = new CrewMember();
        isa.withName("Isa Miranda").withRole(Role.ACTOR);
        isa.setCanonicalUri("http://gb.netflix.com/people/64305");
        CrewMember liliana = new CrewMember();
        liliana.withName("Liliana Cavani").withRole(Role.DIRECTOR);
        liliana.setCanonicalUri("http://gb.netflix.com/people/15506");
        
        assertThat(film.getPeople().size(), is(7));
        assertEquals(ImmutableList.copyOf(film.getPeople()), ImmutableList.of(dirk, charlotte, philippe, gabriele, giuseppe, isa, liliana));
        
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
