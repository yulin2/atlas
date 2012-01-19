package org.atlasapi.remotesite.itunes.epf.model;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public enum CollectionType {

    TV_SEASON("6"),
    IOS_AUDIO_TONE("9"),
    DESKTOP_APP_BUNDLE("10"),
    MOVIE_BUNDLE("8"),
    RINGTONE("7"),
    COMPILATION("4"),
    ALBUM("1"),
    MAXI_SINGLE("2"),
    ORPHAN("3"),
    AUDIOBOOK("5");
    
    private String id;

    private CollectionType(String id) {
        this.id = id;
    }

    public static final Function<CollectionType, String> TO_ID = new Function<CollectionType, String>() {
        @Override
        public String apply(CollectionType input) {
            return input.getId();
        }
    };

    private static final Map<String, CollectionType> idMap = Maps.uniqueIndex(ImmutableList.copyOf(values()), TO_ID);
    
    public static CollectionType collectionTypeForId(String id) {
        return idMap.get(id);
    }

    public static final Function<String, CollectionType> FROM_ID = new Function<String, CollectionType>() {
        @Override
        public CollectionType apply(String input) {
            return collectionTypeForId(input);
        }
    };

    public String getId() {
        return id;
    }

}
