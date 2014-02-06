package org.atlasapi.remotesite.rovi;

import java.util.Collection;
import java.util.Map;

import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;


public class CultureToPublisherMap {
    
    private static final String ENGLISH_LANG = "en";
    private static final String FRENCH_LANG = "fr";
    private static final String ENGLISH_UK_CULTURE = "English - UK";
    private static final String ENGLISH_NA_CULTURE = "English - NA";
    private static final String FRENCH_FR_CULTURE = "French Generic";
    private static final String FRENCH_CA_CULTURE = "French - Québec";

    private static final Multimap<String, String> languageToCultures = HashMultimap.create();
    private static final Map<String, Publisher> cultureToPublisher = Maps.newHashMap();
    
    static {
        languageToCultures.put(ENGLISH_LANG, ENGLISH_UK_CULTURE);
        languageToCultures.put(ENGLISH_LANG, ENGLISH_NA_CULTURE);
        languageToCultures.put(FRENCH_LANG, FRENCH_FR_CULTURE);
        languageToCultures.put(FRENCH_LANG, FRENCH_CA_CULTURE);
        
        cultureToPublisher.put(ENGLISH_NA_CULTURE, Publisher.ROVI_EN_US);
        cultureToPublisher.put(ENGLISH_UK_CULTURE, Publisher.ROVI_EN_GB);
        cultureToPublisher.put(FRENCH_FR_CULTURE, Publisher.ROVI_FR_FR);
        cultureToPublisher.put(FRENCH_CA_CULTURE, Publisher.ROVI_FR_CA);
    }
    
    public static Optional<String> getDefaultCultureForLanguage(String language) {
        if (language.equals(ENGLISH_LANG)) {
            return Optional.of(ENGLISH_UK_CULTURE);
        }
        
        if (language.equals(FRENCH_LANG)) {
            return Optional.of(FRENCH_FR_CULTURE);
        }
        
        return Optional.absent();
    }
    
    public static Collection<String> getCultures(String language) {
        return languageToCultures.get(language.toLowerCase());
    }
    
    public static Publisher getPublisher(String culture) {
        return cultureToPublisher.get(culture);
    }
    
}
