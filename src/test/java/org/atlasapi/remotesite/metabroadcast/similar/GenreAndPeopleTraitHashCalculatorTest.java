package org.atlasapi.remotesite.metabroadcast.similar;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.testing.BrandTestDataBuilder;
import org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder;
import org.junit.Test;

import com.google.api.client.util.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;



public class GenreAndPeopleTraitHashCalculatorTest {

    private static final GenreAndPeopleTraitHashCalculator traitHashCalculator = new GenreAndPeopleTraitHashCalculator();
    
    @Test
    public void testHashesIncludeGenres() {
        String aGenre = "http://a.brand/";
        String anotherGenre = "http://b.brand/";
        
        Brand b = BrandTestDataBuilder.brand().build();
        b.setGenres(ImmutableSet.of(aGenre, anotherGenre));
        
        HashFunction hash = Hashing.goodFastHash(32);
        
        Set<Integer> expectedHashes = ImmutableSet.of(
                hash.hashString(aGenre, Charsets.UTF_8).asInt(),
                hash.hashString(anotherGenre, Charsets.UTF_8).asInt());
        
        assertThat(traitHashCalculator.traitHashesFor(b), is(expectedHashes));
    }
    
    @Test
    public void testHashesIncludePeople() {
        String aGenre = "http://a.brand/";
        String anotherGenre = "http://b.brand/";
        String aPerson = "http://crew.memeber";
        
        Item i = ComplexItemTestDataBuilder
                     .complexItem()
                     .build();
        i.setGenres(ImmutableSet.of(aGenre, anotherGenre));
        i.setPeople(ImmutableList.of(new CrewMember(aPerson, null, Publisher.BBC)));
        
        HashFunction hash = Hashing.goodFastHash(32);
        
        Set<Integer> expectedHashes = ImmutableSet.of(
                hash.hashString(aGenre, Charsets.UTF_8).asInt(),
                hash.hashString(anotherGenre, Charsets.UTF_8).asInt(),
                hash.hashString(aPerson, Charsets.UTF_8).asInt());
        
        assertThat(traitHashCalculator.traitHashesFor(i), is(expectedHashes));
    }
}
