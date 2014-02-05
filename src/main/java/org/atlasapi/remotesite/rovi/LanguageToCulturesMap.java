package org.atlasapi.remotesite.rovi;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class LanguageToCulturesMap {

    private static final Multimap<String, String> languageToCultures = HashMultimap.create();
    
    static {
        languageToCultures.put("en", "English - NA");
        languageToCultures.put("en", "English - UK");
        languageToCultures.put("fr", "French Generic");
    }
    
    public static Collection<String> getCulture(String language) {
        return languageToCultures.get(language);
    }
    
}
