package org.atlasapi.remotesite.metabroadcast.similar;

import java.util.Set;

import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


public class GenreAndPeopleTraitHashCalculator implements TraitHashCalculator {

    static final HashFunction hash = Hashing.goodFastHash(32);
    
    @Override
    public Set<Integer> traitHashesFor(Described d) {
        ImmutableSet.Builder<Integer> hashes = ImmutableSet.builder();
        hashes.addAll(Iterables.transform(d.getGenres(), GENRE_HASH));
        
        if (d instanceof Item) {
            hashes.addAll(Iterables.filter(Iterables.transform(((Item) d).getPeople(), CREW_HASH), 
                                           Predicates.notNull()));
        }
        return hashes.build();
    }
    
    private static final Function<String, Integer> GENRE_HASH = new Function<String, Integer>() {

        @Override
        public Integer apply(String s) {
            return hash.hashString(s, Charsets.UTF_8).asInt();
        }
        
    };
    
    private static final Function<CrewMember, Integer> CREW_HASH = new Function<CrewMember, Integer>() {

        @Override
        public Integer apply(CrewMember c) {
            if (c.getCanonicalUri() == null) {
                return null;
            }
            return hash.hashString(c.getCanonicalUri(), Charsets.UTF_8).asInt();
        }
        
    };
}
