package org.atlasapi.output.simple;

import static org.junit.Assert.*;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Person;
import org.atlasapi.output.Annotation;
import org.junit.Test;
import org.mockito.Mockito;


public class PersonModelSimplifierTest {

    private final PersonModelSimplifier personSimplifier = new PersonModelSimplifier(Mockito.mock(ImageSimplifier.class));
    
    @Test
    public void testUsesLowercaseIdGenerator() {
        
        Person person = new Person();
        person.setId(1234l);
        org.atlasapi.media.entity.simple.Person simplePerson = personSimplifier.simplify(person , Annotation.defaultAnnotations(), Mockito.mock(ApplicationConfiguration.class));
        
        String lowercasedId = simplePerson.getId().toLowerCase();
        assertEquals(lowercasedId, simplePerson.getId());
        
    }

}
