package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.feeds.radioplayer.RadioPlayerService;
import org.atlasapi.feeds.radioplayer.RadioPlayerServices;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;

public class BbcIonServices {

    public static BiMap<String, String> services = ImmutableBiMap.<String, String>builder()
        .put("bbc_one_london",  "http://www.bbc.co.uk/services/bbcone/london")
        .put("bbc_two_england", "http://www.bbc.co.uk/services/bbctwo/england")
        .put("bbc_three",       "http://www.bbc.co.uk/services/bbcthree")
        .put("bbc_four",        "http://www.bbc.co.uk/services/bbcfour")
        .put("bbc_parliament",  "http://www.bbc.co.uk/services/parliament")
        .putAll(
            Maps.transformValues(
                Maps.uniqueIndex(RadioPlayerServices.services, new Function<RadioPlayerService, String>() {
                    @Override
                    public String apply(RadioPlayerService input) {
                        return input.getIonId();
                    }
                }), 
                new Function<RadioPlayerService, String>() {
                    @Override
                    public String apply(RadioPlayerService input) {
                        return input.getServiceUri();
                    }
                }
            )
        ).build();
    
    public static String get(String ionService) {
        return services.get(ionService);
    }

    public static String reverseGet(String bbcServiceUri) {
        return services.inverse().get(bbcServiceUri);
    }
    
}
