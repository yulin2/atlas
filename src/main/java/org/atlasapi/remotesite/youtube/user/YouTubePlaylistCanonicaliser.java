package org.atlasapi.remotesite.youtube.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.query.uri.canonical.Canonicaliser;

public class YouTubePlaylistCanonicaliser implements Canonicaliser {

    private static final Pattern PLAYLIST = Pattern.compile("https?:\\/\\/gdata.youtube.com\\/feeds\\/api\\/(.*)playlists\\/(.+)");

    @Override
    public String canonicalise(String uri) {
        Matcher matcher = PLAYLIST.matcher(uri);
        if (matcher.matches()) {
            return uri;
        }
        return null;
    }

    public static String curieFor(String uri) {
        Matcher matcher = PLAYLIST.matcher(uri);
        if (matcher.matches()) {
            return "yt:_playlist_"+matcher.group(2);
        }
        
        return null;
    }
}
