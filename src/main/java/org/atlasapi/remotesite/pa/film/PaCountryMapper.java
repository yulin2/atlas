package org.atlasapi.remotesite.pa.film;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;

public class PaCountryMapper {
    
    private final Splitter splitter = Splitter.on("/").trimResults();
    
    private final Map<String, Country> countryMap;
    
    public PaCountryMapper() {
        countryMap = ImmutableMap.<String, Country>builder()
            .put("UK", Countries.GB)
            .put("US", Countries.US)
            .put("Fr", Countries.FR)
            .put("Ire", Countries.IE)
            .put("It", Countries.IT)
            .build();
    }
    
    public Set<Country> mapToCountries(String countryString) {
        Iterable<String> countryCodes = splitter.split(countryString);
        
        Builder<Country> countries = ImmutableSet.builder();
        
        for (String countryCode : countryCodes) {
            Country country = countryMap.get(countryCode);
            if (country != null) {
                countries.add(country);
            }
            
        }
        
        return countries.build();
    }

}
