package org.atlasapi.remotesite.talktalk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;


public class TalkTalkFilmYearExtractor {

    private static final Pattern FILM_YEAR_PATTERN = Pattern.compile(".*\\((\\d{4})\\)\\(\\d+ ?mins\\).*");
    
    public Optional<Integer> extractYear(String description) {
        if (description == null) {
            return Optional.absent();
        }
        Matcher matcher = FILM_YEAR_PATTERN.matcher(description);
        if (matcher.matches()) {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        }
        return Optional.absent();
    }
}
