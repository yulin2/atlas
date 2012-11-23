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

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.intl.Countries;

public class NetflixBrandParseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testBrandParsing() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-brand.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixContentExtractor<Brand> brandExtractor = new NetflixBrandExtractor();        
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(Mockito.mock(NetflixContentExtractor.class), brandExtractor, Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class));

        assertThat(rootElement.getChildElements().size(), is(1));
        
        Set<? extends Content> contents = extractor.extract(rootElement.getChildElements().get(0));
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(1));
        
        // check that it is a Brand
        Content content = contents.iterator().next();
        assertTrue(content instanceof Brand);
        Brand brand = (Brand) content;
        
        // check contents of item
        assertThat(brand.getCanonicalUri(), equalTo("http://gb.netflix.com/shows/70136130"));
        assertThat(brand.getTitle(), equalTo("Heroes"));
        assertThat(brand.getDescription(), equalTo("Drawing on American comic book tradition," +
        		" this groundbreaking sci-fi serial drama intertwines the stories of a disparate " +
        		"group of seemingly ordinary people who suddenly discover they possess superhuman " +
        		"abilities."));
        assertThat(brand.getYear(), equalTo(2006));

        assertThat(brand.getGenres().size(), is(6));
        assertEquals(brand.getGenres(), ImmutableSet.of(
                "http://gb.netflix.com/genres/tvshows", 
                "http://gb.netflix.com/genres/ustvshows", 
                "http://gb.netflix.com/genres/tvaction&adventure",
                "http://gb.netflix.com/genres/tvdramas",
                "http://gb.netflix.com/genres/tvsci-fi&fantasy",
                "http://gb.netflix.com/genres/ustvdramas"
            ));
        
        CrewMember jackColeman = new CrewMember();
        jackColeman.withName("Jack Coleman").withRole(Role.ACTOR);
        jackColeman.setCanonicalUri("http://gb.netflix.com/people/20000578");
        CrewMember haydenPanettiere = new CrewMember();
        haydenPanettiere.withName("Hayden Panettiere").withRole(Role.ACTOR);
        haydenPanettiere.setCanonicalUri("http://gb.netflix.com/people/20016275");
        CrewMember miloVentimiglia = new CrewMember();
        miloVentimiglia.withName("Milo Ventimiglia").withRole(Role.ACTOR);
        miloVentimiglia.setCanonicalUri("http://gb.netflix.com/people/20048493");
        CrewMember masiOka = new CrewMember();
        masiOka.withName("Masi Oka").withRole(Role.ACTOR);
        masiOka.setCanonicalUri("http://gb.netflix.com/people/30057259");
        CrewMember sendhilRamamurthy = new CrewMember();
        sendhilRamamurthy.withName("Sendhil Ramamurthy").withRole(Role.ACTOR);
        sendhilRamamurthy.setCanonicalUri("http://gb.netflix.com/people/30057260");
        CrewMember jamesKyson = new CrewMember();
        jamesKyson.withName("James Kyson").withRole(Role.ACTOR);
        jamesKyson.setCanonicalUri("http://gb.netflix.com/people/30058990");
        CrewMember adrianPasdar = new CrewMember();
        adrianPasdar.withName("Adrian Pasdar").withRole(Role.ACTOR);
        adrianPasdar.setCanonicalUri("http://gb.netflix.com/people/71663");
        CrewMember zacharyQuinto = new CrewMember();
        zacharyQuinto.withName("Zachary Quinto").withRole(Role.ACTOR);
        zacharyQuinto.setCanonicalUri("http://gb.netflix.com/people/30013156");
        CrewMember gregGrunberg = new CrewMember();
        gregGrunberg.withName("Greg Grunberg").withRole(Role.ACTOR);
        gregGrunberg.setCanonicalUri("http://gb.netflix.com/people/20007498");
        CrewMember aliLarter = new CrewMember();
        aliLarter.withName("Ali Larter").withRole(Role.ACTOR);
        aliLarter.setCanonicalUri("http://gb.netflix.com/people/20007804");
        CrewMember cristineRose = new CrewMember();
        cristineRose.withName("Cristine Rose").withRole(Role.ACTOR);
        cristineRose.setCanonicalUri("http://gb.netflix.com/people/20059195");
        CrewMember ashleyCrow = new CrewMember();
        ashleyCrow.withName("Ashley Crow").withRole(Role.ACTOR);
        ashleyCrow.setCanonicalUri("http://gb.netflix.com/people/20033069");
        CrewMember jimmyJeanLouis = new CrewMember();
        jimmyJeanLouis.withName("Jimmy Jean-Louis").withRole(Role.ACTOR);
        jimmyJeanLouis.setCanonicalUri("http://gb.netflix.com/people/20035856");
        CrewMember robertKnepper = new CrewMember();
        robertKnepper.withName("Robert Knepper").withRole(Role.ACTOR);
        robertKnepper.setCanonicalUri("http://gb.netflix.com/people/20009942");
        CrewMember madelineZima = new CrewMember();
        madelineZima.withName("Madeline Zima").withRole(Role.ACTOR);
        madelineZima.setCanonicalUri("http://gb.netflix.com/people/20031341");
        CrewMember rayPark = new CrewMember();
        rayPark.withName("Ray Park").withRole(Role.ACTOR);
        rayPark.setCanonicalUri("http://gb.netflix.com/people/20014655");
        CrewMember kristenBell = new CrewMember();
        kristenBell.withName("Kristen Bell").withRole(Role.ACTOR);
        kristenBell.setCanonicalUri("http://gb.netflix.com/people/20052049");
        CrewMember tawnyCypress = new CrewMember();
        tawnyCypress.withName("Tawny Cypress").withRole(Role.ACTOR);
        tawnyCypress.setCanonicalUri("http://gb.netflix.com/people/30057262");
        CrewMember leonardRoberts = new CrewMember();
        leonardRoberts.withName("Leonard Roberts").withRole(Role.ACTOR);
        leonardRoberts.setCanonicalUri("http://gb.netflix.com/people/185202");
        CrewMember timKring = new CrewMember();
        timKring.withName("Tim Kring").withRole(Role.WRITER);
        timKring.setCanonicalUri("http://gb.netflix.com/people/30141131");
        
        assertThat(brand.people().size(), is(20));
        assertEquals(ImmutableList.copyOf(brand.people()), ImmutableList.of(jackColeman, haydenPanettiere, 
                miloVentimiglia, masiOka, sendhilRamamurthy, jamesKyson, adrianPasdar, zacharyQuinto, 
                gregGrunberg, aliLarter, cristineRose, ashleyCrow, jimmyJeanLouis, robertKnepper, 
                madelineZima, rayPark, kristenBell, tawnyCypress, leonardRoberts, timKring
            ));
        
        assertThat(brand.getCertificates().size(), is(1));
        for (Certificate cert : brand.getCertificates()) {
            assertThat(cert.classification(), equalTo("15"));
            assertThat(cert.country(), equalTo(Countries.GB));
        }
        
        assertThat(brand.getAliases().size(), is(1));
        for (String alias : brand.getAliases()) {
            assertThat(alias, equalTo("http://api.netflix.com/catalog/titles/series/70136130"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBrandParsingNoLongSynopsis() {
        Document netflixData;
        try {
            netflixData = new Builder().build(new ClassPathResource("netflix-brand-short-synopsis.xml").getInputStream());
        } catch (Exception e) {
            fail("Exception " + e + " was thrown while opening the test file");
            // will never reach here;
            return;
        }
        
        Element rootElement = netflixData.getRootElement();
        
        NetflixContentExtractor<Brand> brandExtractor = new NetflixBrandExtractor();        
        NetflixXmlElementContentExtractor extractor = new NetflixXmlElementContentExtractor(Mockito.mock(NetflixContentExtractor.class), brandExtractor, Mockito.mock(NetflixContentExtractor.class), Mockito.mock(NetflixContentExtractor.class));

        assertThat(rootElement.getChildElements().size(), is(1));
        
        Set<? extends Content> contents = extractor.extract(rootElement.getChildElements().get(0));
        // check that a piece of content is returned
        assertFalse(contents.isEmpty());
        assertThat(contents.size(), is(1));
        
        // check that it is a Film
        Content content = contents.iterator().next();
        assertTrue(content instanceof Brand);
        Brand brand = (Brand) content;
        
        // check contents of item
        assertThat(brand.getCanonicalUri(), equalTo("http://gb.netflix.com/shows/70136130"));
        assertThat(brand.getTitle(), equalTo("Heroes"));
        assertThat(brand.getDescription(), equalTo("This groundbreaking sci-fi drama intertwines" +
        		" the stories of a disparate group of people who suddenly discover they possess" +
        		" superhuman abilities."));
    }
}
