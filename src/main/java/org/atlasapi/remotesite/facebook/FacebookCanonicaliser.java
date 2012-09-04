package org.atlasapi.remotesite.facebook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.query.uri.canonical.Canonicaliser;

public class FacebookCanonicaliser implements Canonicaliser {
    
    public static final String CANONICAL_PREFIX = "http://graph.facebook.com/";
    private final Pattern graphUriPattern = Pattern.compile("https?://graph.facebook.com/(.+)");

    @Override
    public String canonicalise(String uri) {
        Matcher matcher = graphUriPattern.matcher(uri);
        if (matcher.matches()) {
            return CANONICAL_PREFIX+matcher.group(1);
        }
        return null;
    }

}
