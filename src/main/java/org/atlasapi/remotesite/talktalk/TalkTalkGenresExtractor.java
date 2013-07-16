package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.GenreListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.GenreType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;

import com.google.common.collect.ImmutableSet;

/**
 * Extracts genre strings from TalkTalk {@link ItemDetailType} according to <a
 * href="http://docs.metabroadcast.com/display/mbst/TalkTalk+VOD">http://docs.
 * metabroadcast.com/display/mbst/TalkTalk+VOD</a>
 * 
 */
public class TalkTalkGenresExtractor {

    private static final String GENRE_URI_PATTERN = "http://talktalk.net/genres/%s";

    public Iterable<String> extract(ItemDetailType entity) {
        ImmutableSet.Builder<String> genres = ImmutableSet.builder();
        for (GenreListType genre : entity.getGenreList()) {
            for (GenreType genreType : genre.getGenre()) {
                genres.add(String.format(GENRE_URI_PATTERN, genreType.getGenreCode()));
            }
        }
        return genres.build();
    }
    
}
