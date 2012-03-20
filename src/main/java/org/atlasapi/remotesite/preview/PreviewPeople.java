package org.atlasapi.remotesite.preview;

import org.atlasapi.media.content.Actor;
import org.atlasapi.media.content.CrewMember;
import org.atlasapi.media.content.Publisher;

public class PreviewPeople {
    private PreviewPeople() {
    }
    
    public static Actor actor(String id, String name, String character) {
        return Actor.actor("actor_" + id, name, character, Publisher.PREVIEW_NETWORKS);
    }
    
    public static CrewMember crewMember(String type, String id, String name, String role) {
        return CrewMember.crewMember(type + "_" + id, name, role, Publisher.PREVIEW_NETWORKS);
    }
}
