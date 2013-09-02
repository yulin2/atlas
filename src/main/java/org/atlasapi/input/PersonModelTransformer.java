package org.atlasapi.input;

import org.atlasapi.media.entity.simple.Person;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.Clock;

public class PersonModelTransformer extends DescribedModelTransformer<Person, org.atlasapi.media.entity.Person> {

    public PersonModelTransformer(Clock clock) {
        super(clock);
    }
    
    @Override
    protected org.atlasapi.media.entity.Person createDescribedOutput(Person simple, DateTime now) {
        org.atlasapi.media.entity.Person person = new org.atlasapi.media.entity.Person();
        
        person.setGivenName(simple.getGivenName());
        person.setFamilyName(simple.getFamilyName());
        person.setGender(simple.getGender());
        person.setBirthDate(simple.getBirthDate());
        person.setBirthPlace(simple.getBirthPlace());
        person.setQuotes(simple.getQuotes());
        
        return person;
    }

    
}
