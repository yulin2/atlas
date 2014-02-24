package org.atlasapi.remotesite.rovi;


public class RoviCanonicalUriGenerator {

    public static String canonicalUriForProgram(String id) {
        return "http://rovicorp.com/programs/".concat(id);
    }

    public static String canonicalUriForSeason(String id) {
        return "http://rovicorp.com/seasons/".concat(id);
    }

    public static String canonicalUriForSeasonHistory(String id) {
        return "http://rovicorp.com/season-histories/".concat(id);
    }
    
}
