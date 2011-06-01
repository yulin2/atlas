package org.atlasapi.remotesite.pa;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Maps;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;

public class PaCountryMap {
    
    private final Splitter splitter = Splitter.on("/");
    
    private final Map<String, Country> codeToCountry = Maps.newHashMap();
    
    public PaCountryMap() {
        codeToCountry.put("UK", Countries.GB);
        codeToCountry.put("Fr", Countries.FR);
        codeToCountry.put("US", Countries.US);
        codeToCountry.put("Ire", Countries.IE);
        codeToCountry.put("It", Countries.IT);
    }
    
    public Set<Country> parseCountries(String countryField) {
        Iterable<String> countryCodes = splitter.split(countryField);
        
        Builder<Country> results = ImmutableSet.builder();
        for (String countryCode : countryCodes) {
            Country country = codeToCountry.get(countryCode);
            if (country != null) {
                results.add(country);
            }
        }
        
        return results.build();
    }
    
}
