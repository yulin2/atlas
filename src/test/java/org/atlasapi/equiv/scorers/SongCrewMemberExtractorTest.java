package org.atlasapi.equiv.scorers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class SongCrewMemberExtractorTest {

    private final SongCrewMemberExtractor extractor = new SongCrewMemberExtractor();
    
    @Test
    public void testExtractsStandardPeople() {
        Item song = song("Sultans of Swing", crew("Dire Straits"));
        List<CrewMember> people = extractor.apply(song);
        assertThat(people.size(), is(1));
        assertThat(names(people), hasItem("Dire Straits"));
    }
    
    @Test
    public void testExtractsPeopleFromAmpersandSeparatedArtistNames() {
        Item song = song("Area Codes", crew("J.Period & Nate Dogg"));
        List<CrewMember> people = extractor.apply(song);
        assertThat(people.size(), is(2));
        assertThat(names(people), hasItem("J.Period"));
        assertThat(names(people), hasItem("Nate Dogg"));
    }

    @Test
    public void testExtractsPeopleFromCommaSeparatedFeaturing() {
        Item song = song("Roar (Paperbwoy Remix feat. Lethal B, JME &amp; Scrufizzer)", crew("Nate Dogg"));
        List<CrewMember> people = extractor.apply(song);
        assertThat(people.size(), is(4));
        assertThat(names(people), hasItem("Lethal B"));
        assertThat(names(people), hasItem("JME"));
        assertThat(names(people), hasItem("Scrufizzer"));
        assertThat(names(people), hasItem("Nate Dogg"));
    }
    
    @Test
    public void testExtractsFeaturedPeopleFromSongTitle() {
        Item song = song("Area Codes (feat. Ludacris)", crew("J.Period & Nate Dogg"));
        List<CrewMember> people = extractor.apply(song);
        assertThat(people.size(), is(3));
        assertThat(names(people), hasItem("J.Period"));
        assertThat(names(people), hasItem("Nate Dogg"));
        assertThat(names(people), hasItem("Ludacris"));
    }
    
    private Iterable<String> names(Iterable<CrewMember> members) {
        return Iterables.transform(members, new Function<CrewMember, String>() {
            @Override
            public String apply(@Nullable CrewMember input) {
                return input.name();
            }
        });
    }

    private CrewMember crew(String name) {
        CrewMember member = new CrewMember().withName(name);
        member.setCanonicalUri(name);
        return member;
    }

    private Item song(String title, CrewMember... crew) {
        Item item = new Item();
        item.setPeople(ImmutableList.copyOf(crew));
        item.setTitle(title);
        return item;
    }
}
