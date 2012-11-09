package org.atlasapi.equiv.generators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Function;

public class SongTitleTransform implements Function<String, String> {

    private static final Pattern FEAT_PATTERN = Pattern.compile("(.*) \\(?(f|F)eat(|uring).*\\)?.*");

    @Override
    @Nullable
    public String apply(@Nullable String input) {
        if (input == null) {
            return null;
        }
        return santize(input);
    }

    private String santize(String input) {
        Matcher matcher = FEAT_PATTERN.matcher(input);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return input;
    }

}
