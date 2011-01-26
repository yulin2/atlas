package org.atlasapi.remotesite.bbc.ion;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public class BbcIonServiceMap {

    private static BiMap<String, String> services = ImmutableBiMap.<String, String>builder()
        .put("bbc_radio_one", "http://www.bbc.co.uk/services/radio1/england")
        .build();
    
    public static String get(String ionService) {
        return services.get(ionService);
    }

    public static String reverseGet(String bbcServiceUri) {
        return services.inverse().get(bbcServiceUri);
    }
    
}
