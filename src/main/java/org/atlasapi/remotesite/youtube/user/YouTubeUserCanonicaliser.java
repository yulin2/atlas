package org.atlasapi.remotesite.youtube.user;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.query.uri.canonical.Canonicaliser;

import com.google.common.collect.Lists;

public class YouTubeUserCanonicaliser implements Canonicaliser {

    private static final List<Pattern> alternateUris = Lists.newArrayList(Pattern.compile("https?:\\/\\/.*\\.?youtube.com\\/user\\/(\\w+).*"), Pattern
            .compile("https?:\\/\\/.*\\.?youtube.com\\/feeds/api/users/(\\w+)/playlists"));

    private String canonicalUriFor(String videoId) {
        return "http://www.youtube.com/user/" + videoId;
    }
    
    public static String apiUrlFrom(String uri) {
        String programmeId = videoIdFrom(uri);
        if (programmeId == null) {
            return null;
        }
        
        return "http://gdata.youtube.com/feeds/api/users/"+programmeId+"/playlists";
    }

    public static String videoIdFrom(String uri) {
        for (Pattern p : alternateUris) {
            Matcher matcher = p.matcher(uri);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @Override
    public String canonicalise(String alternate) {
        String programmeId = videoIdFrom(alternate);
        if (programmeId == null) {
            return null;
        }
        return canonicalUriFor(programmeId);
    }

    // Curie [ yt:abcd ]
    public static String curieFor(String uri) {
        String videoId = videoIdFrom(uri);
        return "yt:user_" + videoId;
    }
}
