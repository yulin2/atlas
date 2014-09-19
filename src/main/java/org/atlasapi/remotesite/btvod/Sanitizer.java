package org.atlasapi.remotesite.btvod;

import static org.scribe.utils.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;

public class Sanitizer {

    public static String sanitize(String input) {
        checkNotNull(input, "Can't sanitize a null input");

        String sanitized = CharMatcher.JAVA_LETTER_OR_DIGIT
                .or(CharMatcher.WHITESPACE)
                .retainFrom(input);

        sanitized = CharMatcher.WHITESPACE.trimAndCollapseFrom(sanitized, ' ');

        return CharMatcher.WHITESPACE.replaceFrom(sanitized, '-').toLowerCase();
    }
}
