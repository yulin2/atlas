package org.atlasapi.remotesite.itunes.epf.model;

import java.util.Map;

import org.atlasapi.remotesite.itunes.epf.model.ArtistType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public enum ArtistType {
    ARTIST("1"),
    TV_SHOW("2"),
    STUDIO("3"),
    PODCAST_ARTIST("4"),
    AUTHOR("5"),
    SOFTWARE_ARTIST("7"),
    ITUNES_U_ARTIST("8"),
    MOVIE_ARTIST("6");
    
    private final String id;

    ArtistType(String id) {
        this.id = id;
    }
    
    public static final Function<ArtistType, String> TO_ID = new Function<ArtistType, String>() {
        @Override
        public String apply(ArtistType input) {
            return input.getId();
        }
    };
    
    private static final Map<String, ArtistType> idMap = Maps.uniqueIndex(ImmutableList.copyOf(values()), TO_ID);

    public static ArtistType artistTypeForId(String id) {
        return idMap.get(id);
    }
    
    public static final Function<String, ArtistType> FROM_ID = new Function<String, ArtistType>() {
        @Override
        public ArtistType apply(String input) {
            return artistTypeForId(input);
        }
    };

    public String getId() {
        return id;
    }
};
