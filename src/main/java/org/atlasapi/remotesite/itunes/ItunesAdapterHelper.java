package org.atlasapi.remotesite.itunes;

public class ItunesAdapterHelper {
    public static final String LOOKUP_URL_BASE = "http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStoreServices.woa/wa/wsLookup?limit=200&country=gb&media=tvShow";
    
    public String getCurie(long entityId) {
        return "itunes:" + entityId;
    }
    
    public String getGenreUri(int genreId) {
        return "http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=" + genreId;
    }
}
