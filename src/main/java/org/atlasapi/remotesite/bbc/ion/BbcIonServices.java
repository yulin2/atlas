package org.atlasapi.remotesite.bbc.ion;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class BbcIonServices {

    public static BiMap<String, String> services = ImmutableBiMap.<String, String>builder()
        .put("bbc_one_london", "http://www.bbc.co.uk/services/bbcone/london")
        .build();
    
    public static String get(String ionService) {
        return services.get(ionService);
    }

    public static String reverseGet(String bbcServiceUri) {
        return services.inverse().get(bbcServiceUri);
    }
    
}
