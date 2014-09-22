package org.atlasapi.remotesite.btvod;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.CharMatcher;

public class Sanitizer {

    public static String sanitize(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return input;
        }

        String sanitized = CharMatcher.JAVA_LETTER_OR_DIGIT
                .or(CharMatcher.WHITESPACE)
                .retainFrom(input);

        sanitized = CharMatcher.WHITESPACE.trimAndCollapseFrom(sanitized, ' ');

        return CharMatcher.WHITESPACE.replaceFrom(sanitized, '-').toLowerCase();
    }
}
