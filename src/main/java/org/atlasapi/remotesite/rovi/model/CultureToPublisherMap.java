package org.atlasapi.remotesite.rovi.model;

import java.util.Collection;

import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;


public class CultureToPublisherMap {
    
    private static final String ENGLISH_LANG = "en";
    private static final String FRENCH_LANG = "fr";
    protected static final String ENGLISH_UK_CULTURE = "English - UK";
    protected static final String ENGLISH_NA_CULTURE = "English - NA";
    protected static final String FRENCH_FR_CULTURE = "French Generic";
    protected static final String FRENCH_CA_CULTURE = "French - Québec";

    private static final Multimap<String, String> languageToCultures = HashMultimap.create();
    private static final BiMap<String, Publisher> cultureToPublisher = HashBiMap.create();
    
    private static final Ordering<String> CULTURES_ORDERING = Ordering.natural()
            .onResultOf(new Function<String, Integer>() {
                public Integer apply(String culture) {
                    if (culture.equals(ENGLISH_UK_CULTURE)) {
                        return 1;
                    }
                    if (culture.equals(ENGLISH_NA_CULTURE)) {
                        return 2;
                    }
                    if (culture.equals(FRENCH_FR_CULTURE)) {
                        return 3;
                    }
                    if (culture.equals(FRENCH_CA_CULTURE)) {
                        return 4;
                    }

                    return 5;
                }
            });
    
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
    
    public static String getCulture(Publisher publisher) {
        return cultureToPublisher.inverse().get(publisher);
    }
    
    public static boolean isCultureGoodForPublisher(String culture, Publisher publisher) {
        if (cultureToPublisher.containsValue(publisher)) {
            if (cultureToPublisher.containsKey(culture)) {
                return cultureToPublisher.get(culture).equals(publisher);
            }
            
            return false;
        }
        
        return true;
    }
    
    public static Ordering<String> culturesOrdering() {
        return CULTURES_ORDERING;
    }
    
}
