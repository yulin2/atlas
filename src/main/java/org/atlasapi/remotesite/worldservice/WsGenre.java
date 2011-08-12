package org.atlasapi.remotesite.worldservice;

import java.util.Map;
import java.util.Set;

import org.atlasapi.genres.AtlasGenre;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public enum WsGenre {

    ARD("Arts and Drama", "http://wsarchive.bbc.co.uk/genres/ARD", AtlasGenre.DRAMA),
    BUF("Business and Finance", "http://wsarchive.bbc.co.uk/genres/BUF", AtlasGenre.FACTUAL),
    ENT("Entertainment", "http://wsarchive.bbc.co.uk/genres/ENT", AtlasGenre.ENTERTAINMENT),
    FAV("Faith and Values", "http://wsarchive.bbc.co.uk/genres/FAV", AtlasGenre.FACTUAL),
    LEA("Learning", "http://wsarchive.bbc.co.uk/genres/LEA", AtlasGenre.LEARNING),
    MUS("Music", "http://wsarchive.bbc.co.uk/genres/MUS", AtlasGenre.MUSIC),
    NCA("News & Current Affairs", "http://wsarchive.bbc.co.uk/genres/NCA", AtlasGenre.NEWS),
    PPL("People and Places", "http://wsarchive.bbc.co.uk/genres/PPL", AtlasGenre.FACTUAL),
    SCH("Science and Health", "http://wsarchive.bbc.co.uk/genres/SCH", AtlasGenre.FACTUAL),
    SPO("Sport", "http://wsarchive.bbc.co.uk/genres/SPO", AtlasGenre.SPORT);
    
    private final String title;
    private final String uri;
    private final AtlasGenre atlasGenre;

    WsGenre(String title, String uri, AtlasGenre atlasGenre) {
        this.title = title;
        this.uri = uri;
        this.atlasGenre = atlasGenre;
    }
    
    public String code() {
        return this.toString();
    }
    
    public String title() {
        return this.title;
    }
    
    public String uri() {
        return this.uri;
    }
    
    public AtlasGenre atlasGenre() {
        return this.atlasGenre;
    }
    
    public static Set<String> genresForCode(String code) {
        try {
            WsGenre genre = WsGenre.valueOf(code);
            return ImmutableSet.of(genre.uri(), genre.atlasGenre().getUri());
        } catch (Exception e) {
            return ImmutableSet.of();
        }
    }

    private static Map<String, WsGenre> uriGenreMap = Maps.uniqueIndex(ImmutableSet.copyOf(WsGenre.values()), new Function<WsGenre, String>() {

        @Override
        public String apply(WsGenre input) {
            return input.uri();
        }
    });
    
    public static Maybe<WsGenre> genreForUri(String uri) {
        return Maybe.fromPossibleNullValue(uriGenreMap.get(uri));
    }
}
