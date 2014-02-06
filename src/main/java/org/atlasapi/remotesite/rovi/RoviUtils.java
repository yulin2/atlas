package org.atlasapi.remotesite.rovi;

import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import com.google.common.collect.Iterables;

public class RoviUtils {
    
    public static Publisher getPublisherForLanguage(String language) {
        return Publisher.valueOf("ROVI_" + language.toUpperCase());
    }
    
    public static String canonicalUriFor(String id) {
        return "http://rovicorp.com/programs/".concat(id);
    }
    
    public static String getPartAtPosition(Iterable<String> parts, int pos) {
        return Iterables.get(parts, pos);
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
