package org.atlasapi.output.simple;

import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.simple.Person;

public class CrewMemberSimplifier extends IdentifiedModelSimplifier<CrewMember,Person> {

    public Person apply(CrewMember fullCrew) {
        Person person = new Person();
        person.setType(Person.class.getSimpleName());
        if (fullCrew instanceof Actor) {
            Actor fullActor = (Actor) fullCrew;
            person.setCharacter(fullActor.character());
        }
        
        copyIdentifiedAttributesTo(fullCrew, person);
        person.setName(fullCrew.name());
        person.setProfileLinks(fullCrew.profileLinks());
        person.setRole(fullCrew.role().key());
        
        return person;
    }
    
}
