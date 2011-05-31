package org.atlasapi.remotesite.itunes;

import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;

public class ItunesAdapterHelper {
    public static final String LOOKUP_URL_BASE = "http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStoreServices.woa/wa/wsLookup?limit=200&media=tvShow";
    public static final String COUNTRY_UK = "&country=gb";
    
    public enum ItunesRegion {
        UK("gb", Countries.GB),
        USA("us", Countries.US);
        
        private static final String SEARCH_PARAM = "&country=";
        
        private final String countryCode;
        private final Country country;

        private ItunesRegion(String countryCode, Country country) {
            this.countryCode = countryCode;
            this.country = country;
        }
        
        public String getSearchArgument() {
            return SEARCH_PARAM + countryCode;
        }
        
        public Country getCountry() {
            return country;
        }
    }
    
    public String getCurie(long entityId) {
        return "itunes:" + entityId;
    }
    
    public int getIdFromCurie(String curie) {
        return Integer.valueOf(curie.substring("itunes:".length()));
    }
    
    public String getGenreUri(int genreId) {
        return "http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=" + genreId;
    }
}
