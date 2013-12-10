package org.atlasapi.remotesite.wikipedia;

import java.util.List;

import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.wikipedia.SwebleHelper.ListItemResult;

import com.google.common.collect.ImmutableList;

public final class ExtractionHelper {
    public static void extractCrewMembers(ImmutableList<ListItemResult> from, Role role, List<CrewMember> into) {
        if (from == null) {
            return;
        }
        for (ListItemResult person : from) {
            if (person.articleTitle.isPresent()) {
                into.add(new CrewMember(Article.urlFromTitle(person.articleTitle.get()), null, Publisher.WIKIPEDIA).withRole(role).withName(person.name));
            } else {
                into.add(new CrewMember().withRole(role).withName(person.name).withPublisher(Publisher.WIKIPEDIA));
            }
        }
    }

}
