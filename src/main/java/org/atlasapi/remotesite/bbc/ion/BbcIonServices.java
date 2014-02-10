package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.feeds.radioplayer.RadioPlayerService;
import org.atlasapi.feeds.radioplayer.RadioPlayerServices;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;

public class BbcIonServices {
    
    public static BiMap<String, String> tvServices = ImmutableBiMap.<String, String>builder()
    
//        .put("bbc_one",  "http://www.bbc.co.uk/services/bbcone")
        .put("bbc_one_london",  "http://www.bbc.co.uk/services/bbcone/london")
        .put("bbc_one_west",  "http://www.bbc.co.uk/services/bbcone/west")
        .put("bbc_one_south_west",  "http://www.bbc.co.uk/services/bbcone/south_west")
        .put("bbc_one_south",  "http://www.bbc.co.uk/services/bbcone/south")
        .put("bbc_one_south_east",  "http://www.bbc.co.uk/services/bbcone/south_east")
        .put("bbc_one_west_midlands",  "http://www.bbc.co.uk/services/bbcone/west_midlands")
        .put("bbc_one_east_midlands",  "http://www.bbc.co.uk/services/bbcone/east_midlands")
        .put("bbc_one_east",  "http://www.bbc.co.uk/services/bbcone/east")
        .put("bbc_one_north_east",  "http://www.bbc.co.uk/services/bbcone/north_east")
        .put("bbc_one_north_west",  "http://www.bbc.co.uk/services/bbcone/north_west")
        .put("bbc_one_scotland",  "http://www.bbc.co.uk/services/bbcone/scotland")
        .put("bbc_one_yorks",  "http://www.bbc.co.uk/services/bbcone/yorkshire")
        .put("bbc_one_oxford",  "http://www.bbc.co.uk/services/bbcone/oxford")
        .put("bbc_one_cambridge",  "http://www.bbc.co.uk/services/bbcone/cambridge")
        .put("bbc_one_channel_islands",  "http://www.bbc.co.uk/services/bbcone/channel_islands")
        .put("bbc_one_east_yorkshire",  "http://www.bbc.co.uk/services/bbcone/east_yorkshire")
        .put("bbc_one_northern_ireland",  "http://www.bbc.co.uk/services/bbcone/ni")
        .put("bbc_one_wales",  "http://www.bbc.co.uk/services/bbcone/wales")
        
//        .put("bbc_two", "http://www.bbc.co.uk/services/bbctwo")
        .put("bbc_two_england", "http://www.bbc.co.uk/services/bbctwo/england")
        .put("bbc_two_wales", "http://www.bbc.co.uk/services/bbctwo/wales_analogue")
        .put("bbc_two_wales_digital", "http://www.bbc.co.uk/services/bbctwo/wales")
        .put("bbc_two_northern_ireland", "http://www.bbc.co.uk/services/bbctwo/ni_analogue")
        .put("bbc_two_northern_ireland_digital", "http://www.bbc.co.uk/services/bbctwo/ni")
        
        .put("bbc_three",       "http://www.bbc.co.uk/services/bbcthree")
        .put("bbc_four",        "http://www.bbc.co.uk/services/bbcfour")
        
        .put("bbc_parliament",  "http://www.bbc.co.uk/services/parliament")
        .put("bbc_news24",  "http://www.bbc.co.uk/services/bbcnews")
        .put("bbc_hd",          "http://www.bbc.co.uk/services/bbchd")
        .put("cbbc",          "http://www.bbc.co.uk/services/cbbc")
        .put("cbeebies",          "http://www.bbc.co.uk/services/cbeebies").build();
        
    public static BiMap<String, String> radioServices = ImmutableBiMap.<String, String>builder()
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
        )
        .put("bbc_radio_fourlw", "http://www.bbc.co.uk/services/radio4/lw")
        .build();
    
    public static BiMap<String,String> services = ImmutableBiMap.<String, String>builder().putAll(tvServices).putAll(radioServices).build();
     
    public static String get(String ionService) {
        return services.get(ionService);
    }

    public static String reverseGet(String bbcServiceUri) {
        return services.inverse().get(bbcServiceUri);
    }
    
}
