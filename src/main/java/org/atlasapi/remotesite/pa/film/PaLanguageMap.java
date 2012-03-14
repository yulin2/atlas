package org.atlasapi.remotesite.pa.film;

import java.util.Locale;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class PaLanguageMap {

    private Map<String, Optional<String>> languageMap;

    public PaLanguageMap() {
        this.languageMap = initializeMap();
    }

    private Map<String, Optional<String>> initializeMap() {
        Map<String, Optional<String>> languageCode = Maps.newHashMap();
        for (String code : Locale.getISOLanguages()) {
            languageCode.put(new Locale(code).getDisplayLanguage(Locale.ENGLISH),Optional.of(code));
        }
        return languageCode;
    }
    
    public Optional<String> codeForEnglishLanguageName(String englishName) {
        Optional<String> possibleCode = languageMap.get(englishName);
        return possibleCode == null ? Optional.<String>absent() : possibleCode; 
    }
}
