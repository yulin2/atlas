package org.atlasapi.remotesite.pa.people;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.atlasapi.media.entity.Image;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Person;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.PeopleResolver;
import org.atlasapi.persistence.content.people.PersonWriter;
import org.atlasapi.remotesite.pa.profiles.bindings.Name;
import org.atlasapi.remotesite.pa.profiles.bindings.Picture;
import org.atlasapi.remotesite.pa.profiles.bindings.Pictures;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(MockitoJUnitRunner.class)
public class PaPeopleProcessorTest {
    
    private final PeopleResolver resolver = mock(PeopleResolver.class);
    private final PersonWriter writer = mock(PersonWriter.class);
    private final PaPeopleProcessor processor
        = new PaPeopleProcessor(resolver, writer); 
    
    @Test
    public void testIngestingANewPersonWithoutAnEquivalent() {
        
        org.atlasapi.remotesite.pa.profiles.bindings.Person paPerson
            = new org.atlasapi.remotesite.pa.profiles.bindings.Person();
        paPerson.setId("1");
        Name name = name("David", "Attenborough");
        paPerson.setName(name);
        paPerson.setGender("Male");
        paPerson.setBorn("1926-05-08");
        paPerson.setBornIn("London");
        paPerson.setEarlyLife("early");
        paPerson.setCareer("career");
        paPerson.setQuote("quote");
        Picture picture = picture("image.jpg", 683, 1024);
        paPerson.setPictures(pictures(picture));
        
        when(resolver.person(anyString()))
            .thenReturn(Optional.<Person>absent());
        
        processor.process(paPerson);
        
        ArgumentCaptor<Person> kidnapper = ArgumentCaptor.forClass(Person.class);
        verify(writer).createOrUpdatePerson(kidnapper.capture());
        Person written = kidnapper.getValue();
        
        assertThat(written.getCanonicalUri(), is("http://people.atlasapi.org/people.pressassociation.com/1"));
        assertThat(written.getPublisher(), is(Publisher.PA_PEOPLE));
        
        assertThat(written.getTitle(), is(String.format("%s %s", name.getFirstname(), name.getLastname())));
        assertThat(written.getGivenName(), is(name.getFirstname()));
        assertThat(written.getFamilyName(), is(name.getLastname()));
        
        assertThat(written.getGender(), is(paPerson.getGender()));
        assertThat(written.getBirthDate(), is(new DateTime(1926,5,8,0,0,0,0,DateTimeZones.UTC)));
        assertThat(written.getBirthPlace(), is(paPerson.getBornIn()));
        
        assertThat(written.getDescription(), is(String.format("%s\n\n%s", paPerson.getEarlyLife(), paPerson.getCareer())));
        assertThat(Iterables.getOnlyElement(written.getQuotes()), is("quote"));
        
        Image image = Iterables.getOnlyElement(written.getImages());
        assertThat(image.getCanonicalUri(), is("http://images.atlas.metabroadcast.com/people.pressassociation.com/image.jpg"));
        assertThat(String.valueOf(image.getWidth()), is(picture.getWidth()));
        assertThat(String.valueOf(image.getHeight()), is(picture.getHeight()));
        
        assertTrue(image.getEquivalentTo().isEmpty());
        
        verify(resolver).person(written.getCanonicalUri());
        verify(resolver).person(written.getCanonicalUri().replace(Publisher.PA_PEOPLE.key(), Publisher.PA.key()));
    }
    
    @Test
    public void testIngestingANewPersonWithAnEquivalent() {
        
        org.atlasapi.remotesite.pa.profiles.bindings.Person paPerson
            = new org.atlasapi.remotesite.pa.profiles.bindings.Person();
        paPerson.setId("1");
        Name name = name("David", "Attenborough");
        paPerson.setName(name);
        
        Person equivPerson = new Person();
        equivPerson.setPublisher(Publisher.PA);
        equivPerson.setCanonicalUri("http://people.atlasapi.org/pressassociation.com/1");
        
        String extractedUri = "http://people.atlasapi.org/people.pressassociation.com/1";
        when(resolver.person(equivPerson.getCanonicalUri()))
            .thenReturn(Optional.of(equivPerson));
        when(resolver.person(extractedUri))
            .thenReturn(Optional.<Person>absent());
    
        processor.process(paPerson);
        
        ArgumentCaptor<Person> kidnapper = ArgumentCaptor.forClass(Person.class);
        verify(writer).createOrUpdatePerson(kidnapper.capture());
        Person written = kidnapper.getValue();
        
        assertThat(Iterables.getOnlyElement(written.getEquivalentTo()), is(LookupRef.from(equivPerson)));
        
        verify(resolver).person(extractedUri);
        
    }

    private Pictures pictures(Picture... pictures) {
        Pictures pics = new Pictures();
        Iterables.addAll(pics.getPicture(), Arrays.asList(pictures));
        return pics;
    }

    private Picture picture(String filename, int width, int height) {
        Picture picture = new Picture();
        picture.setvalue(filename);
        picture.setWidth(String.valueOf(width));
        picture.setHeight(String.valueOf(height));
        return picture;
    }

    private Name name(String first, String last) {
        Name name = new Name();
        name.setFirstname(first);
        name.setLastname(last);
        return name;
    }
    
}
