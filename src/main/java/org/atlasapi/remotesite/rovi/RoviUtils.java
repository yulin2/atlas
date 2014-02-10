package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviConstants.DEFAULT_PUBLISHER;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class RoviUtils {
    
    public static Publisher getPublisherForLanguage(String language) {
        return getPublisherForLanguageAndCulture(language, Optional.<String>absent());
    }
    
    
    public static Publisher getPublisherForLanguageAndCulture(String language, Optional<String> descriptionCulture) {
        if (CultureToPublisherMap.getCultures(language).isEmpty()) {
            return Publisher.valueOf("ROVI_" + language.toUpperCase());
        }
        
        if (!descriptionCulture.isPresent()) {
            Optional<String> defaultCulture = CultureToPublisherMap.getDefaultCultureForLanguage(language);
            return CultureToPublisherMap.getPublisher(defaultCulture.get());
        }
        
        Collection<String> cultures = CultureToPublisherMap.getCultures(language);
        if (cultures.contains(descriptionCulture.get())) {
            return CultureToPublisherMap.getPublisher(descriptionCulture.get());
        }
        
        return DEFAULT_PUBLISHER;
    }
    
    public static String canonicalUriForProgram(String id) {
        return "http://rovicorp.com/programs/".concat(id);
    }

    public static String canonicalUriForSeason(String id) {
        return "http://rovicorp.com/seasons/".concat(id);
    }
    
    public static String getPartAtPosition(Iterable<String> parts, int pos) {
        String part = Iterables.get(parts, pos);
        
        return Strings.emptyToNull(part);
    }

    public static Integer getIntPartAtPosition(Iterable<String> parts, int pos) {
        String part = getPartAtPosition(parts, pos);
        
        if (part != null && StringUtils.isNumeric(part)) {
            return Integer.valueOf(part);
        }
        
        return null;
    }

    public static Long getLongPartAtPosition(Iterable<String> parts, int pos) {
        String part = getPartAtPosition(parts, pos);
        
        if (part != null && StringUtils.isNumeric(part)) {
            return Long.valueOf(part);
        }
        
        return null;
    }
    
    public static LocalDate parseDate(String date) {
        int defaultYear = LocalDate.now(DateTimeZone.UTC).getYear();
        int defaultMonth = 01;
        int defaultDay = 01;
        
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));
        
        if (year == 0) {
            year = defaultYear;
        }
        
        if (month == 0) {
            month = defaultMonth;
        }
        
        if (day == 0) {
            day = defaultDay;
        }
        
        return new LocalDate(year, month, day);
    }
    
}
