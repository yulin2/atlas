package org.atlasapi.remotesite.youtube;

import org.atlasapi.query.uri.canonical.Canonicaliser;

public class YouTubeFeedCanonicaliser implements Canonicaliser {
    
    private static final String BASE_URL = "http://gdata.youtube.com/feeds/api/";

    @Override
    public String canonicalise(String uri) {
        return uri != null && uri.startsWith(BASE_URL) ? uri : null;
    }
    
    public static String curieFor(String uri) {
        return "yt:feed_"+uri.replace(BASE_URL, "");
    }
}
