package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.MediaType.AUDIO;
import static org.atlasapi.media.entity.MediaType.VIDEO;
import static org.atlasapi.media.entity.Specialization.RADIO;
import static org.atlasapi.media.entity.Specialization.TV;

import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public class BbcIonMediaTypeMapping {

    private static Set<String> tvServices = ImmutableSet.<String>builder()
        .add("bbc_alba")
        .add("bbc_four")
        .add("bbc_hd")
        .add("bbc_one_cambridge")
        .add("bbc_one_channel_islands")
        .add("bbc_one_east")
        .add("bbc_one_east_midlands")
        .add("bbc_one_east_yorkshire")
        .add("bbc_one_hd")
        .add("bbc_one_london")
        .add("bbc_one_north_east")
        .add("bbc_one_north_west")
        .add("bbc_one_northern_ireland")
        .add("bbc_one_oxford")
        .add("bbc_one_scotland")
        .add("bbc_one_south")
        .add("bbc_one_south_east")
        .add("bbc_one_south_west")
        .add("bbc_one_wales")
        .add("bbc_one_west")
        .add("bbc_one_west_midlands")
        .add("bbc_one_yorks")
        .add("bbc_parliament")
        .add("bbc_three")
        .add("bbc_two_england")
        .add("bbc_two_northern_ireland")
        .add("bbc_two_northern_ireland_digital")
        .add("bbc_two_scotland")
        .add("bbc_two_wales")
        .add("bbc_two_wales_digital")
        .add("cbbc")
        .add("cbeebies")
        .add("bbc_alba")
        .add("bbc_four")
        .add("bbc_hd")
        .add("bbc_one")
        .add("bbc_parliament")
        .add("bbc_three")
        .add("bbc_two")
        .add("cbbc")
        .add("cbeebies").build();

    private static Set<String> radioServices = ImmutableSet.<String>builder()
        .add("bbc_asian_network")
        .add("bbc_radio_five_live")
        .add("bbc_radio_five_live_sports_extra")
        .add("bbc_radio_four_extra")
        .add("bbc_radio_fourfm")
        .add("bbc_radio_fourlw")
        .add("bbc_radio_one")
        .add("bbc_radio_one_northern_ireland")
        .add("bbc_radio_one_scotland")
        .add("bbc_radio_one_wales")
        .add("bbc_radio_three")
        .add("bbc_radio_two")
        .add("bbc_world_service")
        .add("bbc_asian_network")
        .add("bbc_radio_five_live")
        .add("bbc_radio_five_live_sports_extra")
        .add("bbc_radio_four")
        .add("bbc_radio_four_extra")
        .add("bbc_radio_one")
        .add("bbc_radio_three")
        .add("bbc_radio_two")
        .add("bbc_world_service").build();


    private static Map<String, Specialization> specMap = ImmutableMap.<String, Specialization> builder()
            .putAll(mapToSpecialisation(tvServices, TV))
            .putAll(mapToSpecialisation(radioServices, RADIO))
            .build();
    
    private static Map<String, MediaType> mediaTypeMap = ImmutableMap.<String, MediaType> builder()
            .putAll(mapToMediaType(tvServices, VIDEO))
            .putAll(mapToMediaType(radioServices, AUDIO))
            .build();
    
    private static Map<String, Specialization> mapToSpecialisation(Set<String> services, Specialization spec) {
        return Maps.<String, String, Specialization>transformValues(Maps.uniqueIndex(services, Functions.<String>identity()),  Functions.constant(spec));
    }

    private static Map<String, MediaType> mapToMediaType(Set<String> services, MediaType type) {
        return Maps.<String, String, MediaType>transformValues(Maps.uniqueIndex(services, Functions.<String>identity()),  Functions.constant(type));
    }
    
    public static Maybe<Specialization> specialisationForService(String serviceId) {
        return Maybe.fromPossibleNullValue(specMap.get(serviceId));
    }
    
    public static Maybe<MediaType> mediaTypeForService(String serviceId) {
        return Maybe.fromPossibleNullValue(mediaTypeMap.get(serviceId));
    }
}
