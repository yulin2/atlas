package org.atlasapi.remotesite.rovi;

import static com.google.common.base.Preconditions.checkArgument;
import static org.atlasapi.remotesite.rovi.RoviConstants.DEFAULT_PUBLISHER;
import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviConstants.UTF_16LE_BOM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.CultureToPublisherMap;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;

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

    /**
     * Detects if a file starts with an UTF-16LE BOM (Byte Order Mark).
     * UTF-16LE BOM is composed by a sequence of two bytes: 0xFF followed by 0xFE
     * 
     * @param file - The file to analyze
     * @return true if the file starts with an UTF-16LE BOM, false otherwise
     * @throws IOException - if an I/O error occurs
     */
    public static boolean startsWithUTF16LEBom(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        int byte1 = is.read();
        int byte2 = is.read();
        is.close();
        
        int[] readBytes = {byte1, byte2};
        
        return Arrays.equals(readBytes, UTF_16LE_BOM);
    }
    
    /**
     * Strips the UTF-16LE BOM (Byte Order Mark) from a line
     * UTF-16LE BOM is composed by a sequence of two bytes: 0xFF followed by 0xFE
     * 
     * @param line - The line to strip the BOM from
     * @return the line without the UTF-16LE BOM
     */
    public static String stripBom(String line) {
        if (startsWithUTF16LEBom(line)) {
            byte[] bytes = line.getBytes(FILE_CHARSET);
            return new String(stripBomFromBytes(bytes), FILE_CHARSET);
        }
        
        return line;
    }

    /**
     * Detects if a String line starts with an UTF-16LE BOM (Byte Order Mark).
     * UTF-16LE BOM is composed by a sequence of two bytes: 0xFF followed by 0xFE
     * 
     * @param line - The line to analyze
     * @return true if the line starts with an UTF-16LE BOM, false otherwise
     */
    public static boolean startsWithUTF16LEBom(String line) {
        byte[] bytes = line.getBytes(FILE_CHARSET);
        
        if (bytes.length >= UTF_16LE_BOM.length) {
            int[] firstTwoBytes = {toUnsignedInt(bytes[0]), toUnsignedInt(bytes[1])};    
            
            return Arrays.equals(firstTwoBytes, UTF_16LE_BOM);
        }
        
        return false;
    }

    private static byte[] stripBomFromBytes(byte[] bytes) {
        return Arrays.copyOfRange(bytes, UTF_16LE_BOM.length, bytes.length);
    }
    
    private static int toUnsignedInt(byte b) {
        return (int) b & 0xFF;
    }


    public static boolean isEpisodeNumberValid(String episodeNumber) {
        // Restricting to numeric strings and less than 8 character strings (we found some episode numbers that were actually dates)
        return StringUtils.isNumeric(episodeNumber) && episodeNumber.length() < 8;
    }
    
}
