package org.atlasapi.remotesite.worldservice;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public enum WsDataFile {

    AUDIO_ITEM("AudioItem"),
    AUDIO_ITEM_PROG_LINK("AudioItemProgLink"),
    GENRE("Genre"),
    PROGRAMME("Programme"),
    SERIES("Series");
 
    private final String filename;

    WsDataFile(String filename) {
        this.filename = filename;
    }
    
    public String filename(String suffix) {
        return filename + suffix;
    }
    
    @Override
    public String toString() {
        return filename;
    }
    
    private static final Map<String, WsDataFile> fromString = Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<WsDataFile, String>() {
        @Override
        public String apply(WsDataFile input) {
            return input.filename;
        }
    });
    
    public Maybe<WsDataFile> fromString(String filename) {
        return Maybe.fromPossibleNullValue(fromString.get(filename));
    }
}
