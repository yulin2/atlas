package org.atlasapi.equiv.scorers;

import static org.atlasapi.media.entity.CrewMember.Role.ARTIST;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringEscapeUtils;
import org.atlasapi.equiv.generators.SongTitleTransform;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList.Builder;

public class SongCrewMemberExtractor implements Function<Item, List<CrewMember>> {

    Splitter splitter = Splitter.on(CharMatcher.anyOf("&,+"))
            .trimResults()
            .omitEmptyStrings();
    SongTitleTransform titleTransform = new SongTitleTransform();
    
    @Override
    public List<CrewMember> apply(@Nullable Item content) {
        ImmutableSet.Builder<CrewMember> builder = ImmutableSet.builder();
        return builder
                .addAll(peopleFrom(content.getPeople()))
                .addAll(peopleFrom(content.getTitle()))
                .build().asList();
    }
    
    private Iterable<CrewMember> peopleFrom(List<CrewMember> people) {
        Builder<CrewMember> result = ImmutableList.builder();
        for (CrewMember person : people) {
            List<String> names = ImmutableList.copyOf(split(person.name()));
            if (names.size() > 1) {
                result.addAll(membersFrom(names));
            } else {
                result.add(removeThePrefix(person));
            }
        }
        return result.build();
    }

    private Iterable<String> split(String string) {
        String decoded = StringEscapeUtils.unescapeHtml(string);
        return splitter.split(decoded.replaceAll(" and ", " & "));
    }

    private List<CrewMember> membersFrom(Iterable<String> names) {
        Builder<CrewMember> result = ImmutableList.builder();
        for (String name : names) {
            CrewMember member = new CrewMember().withRole(ARTIST).withName(name);
            member.setCanonicalUri(name);
            result.add(removeThePrefix(member));
        }
        return result.build();
    }
    
    private CrewMember removeThePrefix(CrewMember member) {
        if (member.name() != null 
         && (member.name().startsWith("the ") || member.name().startsWith("The "))) {
            member.withName(member.name().substring(4));
        }
        return member;
    }

    private Iterable<CrewMember> peopleFrom(String title) {
        String extractedArtists = titleTransform.extractFeaturedArtists(title);
        return membersFrom(split(extractedArtists));
    }
    
}
