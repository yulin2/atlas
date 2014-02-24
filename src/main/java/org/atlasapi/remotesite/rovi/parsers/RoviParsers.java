package org.atlasapi.remotesite.rovi.parsers;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;


public class RoviParsers {

    public static String getPartAtPosition(Iterable<String> parts, int pos) {
        String part = Iterables.get(parts, pos);
        
        return Strings.emptyToNull(part);
    }
    
    public static ActionType getActionTypeAtPosition(Iterable<String> parts, int pos) {
        String part = Iterables.get(parts, pos);
        return ActionType.fromRoviType(part);
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
    
    /**
     * Convert a String representing a potentially partial date into an instance of ReadablePartial representing the same logic date. 
     * The input string should be 8 characters long and should have the format "yyyyMMdd". 
     * If one or more portions of the date are unknown, they should be filled with zeros (i.e. 20140000) 
     * 
     * @param date - The String representing the date
     * @return an instance of ReadablePartial representing the same logic date
     */
    public static ReadablePartial parsePotentiallyPartialDate(String date) {
        checkArgument(date.length() == 8, "Input date String should be 8 characters long");
        
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));
        
        Partial partial = new Partial();
        
        if (year != 0) {
            partial = partial.with(DateTimeFieldType.year(), year);
        }
        
        if (month != 0) {
            partial = partial.with(DateTimeFieldType.monthOfYear(), month);
        }
        
        if (day != 0) {
            partial = partial.with(DateTimeFieldType.dayOfMonth(), day);
        }
        
        return partial;
    }
    
    public static boolean isEpisodeNumberValid(String episodeNumber) {
        // Restricting to numeric strings and less than 8 character strings (we found some episode numbers that were actually dates)
        return StringUtils.isNumeric(episodeNumber) && episodeNumber.length() < 8;
    }
}
