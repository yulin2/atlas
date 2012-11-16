package org.atlasapi.equiv.generators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Function;

public class SongTitleTransform implements Function<String, String> {

    private static final Pattern FEAT_PATTERN = Pattern.compile("(.*) \\(?((f|F)eat(|uring)|transcribed)\\.? ([^)]+)\\)?.*");

    @Override
    @Nullable
    public String apply(@Nullable String input) {
        if (input == null) {
            return null;
        }
        return removeFeaturedArtists(input);
    }
    
    public String extractFeaturedArtists(String title) {
        Matcher matcher = FEAT_PATTERN.matcher(title);
        if (matcher.matches()) {
            return matcher.group(5);
        }
        return "";
    }

    public String removeFeaturedArtists(String title) {
        Matcher matcher = FEAT_PATTERN.matcher(title);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return title;
    }

}
