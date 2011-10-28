package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonContributor;

import com.metabroadcast.common.base.Maybe;

public class BbcIonContributorPersonExtractor implements ContentExtractor<IonContributor, Maybe<CrewMember>> {

    private static final String ACTOR_ROLE_NAME = "ACTOR";
    private static final String PERSON_BASE_URL = "http://www.bbc.co.uk/people/";
    private static final String PERSON_BASE_CURIE = "bbc:person_";
    
    @Override
    public Maybe<CrewMember> extract(IonContributor contributor) {
        CrewMember person = null;
        String uri = PERSON_BASE_URL+contributor.getId();
        String curie = PERSON_BASE_CURIE+contributor.getId();
        
        if (ACTOR_ROLE_NAME.equals(contributor.getRoleName())) {
            person = new Actor(uri, curie, Publisher.BBC).withCharacter(contributor.getCharacterName()).withRole(Role.ACTOR);
        } else {
            Maybe<Role> role = Role.fromPossibleKey(contributor.getRole().toLowerCase().replace(' ', '_'));
            if (role.isNothing()) {
                return Maybe.nothing();
            }
            person = new CrewMember(uri, curie, Publisher.BBC).withRole(role.requireValue());
        }
        person.withName(contributor.getGivenName()+" "+contributor.getFamilyName()).withProfileLink("http://www.bbc.co.uk"+contributor.getSearchUrl());
        person.setLastUpdated(contributor.getUpdated());
        
        return Maybe.just(person);
    }

}
