package org.atlasapi.remotesite.metabroadcast.picks;

import java.util.Set;

import org.atlasapi.media.util.ItemAndBroadcast;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;


public class GenreMatchPredicate implements Predicate<ItemAndBroadcast> {

    private Set<String> genres;
    
    public GenreMatchPredicate(Iterable<String> genres) {
        this.genres = ImmutableSet.copyOf(genres);
    }
    @Override
    public boolean apply(ItemAndBroadcast input) {
        return !Sets.intersection(input.getItem().getGenres(), genres).isEmpty();
    }

}
