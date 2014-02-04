package org.atlasapi.remotesite.rovi;

import com.google.common.collect.Iterables;

public class RoviUtils {
    
    public static String canonicalUriFor(String id) {
        return "http://rovi.com/program/".concat(id);
    }
    
    public static String getPartAtPosition(Iterable<String> parts, int pos) {
        return Iterables.get(parts, pos);
    }
    
}
