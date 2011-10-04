package org.atlasapi.remotesite.redux;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public final class ReduxServices {

    public static final BiMap<String, Channel> TV_CHANNEL_MAP = ImmutableBiMap.<String,Channel>builder()
            .put("bbcone", Channel.BBC_ONE)
            .put("bbctwo", Channel.BBC_TWO)
            .put("bbcthree", Channel.BBC_THREE)
            .put("bbcfour", Channel.BBC_FOUR)
            .put("cbbc", Channel.CBBC)
            .put("cbeebies", Channel.CBEEBIES)
            .put("bbcnews24", Channel.BBC_NEWS)
            .put("bbchd", Channel.BBC_HD)
            .put("bbcparl", Channel.BBC_PARLIMENT)
            .put("bbconehd", Channel.BBC_ONE_HD)
            .put("itv1", Channel.ITV1_LONDON)
            .put("itv2", Channel.ITV2)
            .put("itv3", Channel.ITV3)
            .put("itv4", Channel.ITV4)
            .put("channel4", Channel.CHANNEL_FOUR)
            .put("e4", Channel.E_FOUR)
            .put("more4", Channel.MORE_FOUR)
            .put("dave", Channel.DAVE)
            .put("five", Channel.FIVE)
    .build();
            
    public static final BiMap<String, Channel> RADIO_CHANNEL_MAP = ImmutableBiMap.<String,Channel>builder()
           .put("bbcr1", Channel.BBC_RADIO_RADIO1)
           .put("bbc1x", Channel.BBC_RADIO_1XTRA)
           .put("bbcr2", Channel.BBC_RADIO_RADIO2)
           .put("bbcr3", Channel.BBC_RADIO_RADIO3)
           .put("bbcr4", Channel.BBC_RADIO_RADIO4)
           .put("bbcr5l", Channel.BBC_RADIO_5LIVE)
           .put("r5lsx", Channel.BBC_RADIO_5LIVESPORTSEXTRA)
           .put("bbc6m", Channel.BBC_RADIO_6MUSIC)
           .put("bbc7", Channel.BBC_RADIO_RADIO4_EXTRA)
           .put("bbcan", Channel.BBC_RADIO_ASIANNETWORK)
           .put("bbcws", Channel.BBC_RADIO_WORLDSERVICE)
   .build();
    
    public static final BiMap<String, Channel> CHANNEL_MAP = ImmutableBiMap.<String,Channel>builder().putAll(TV_CHANNEL_MAP).putAll(RADIO_CHANNEL_MAP).build();
    
    public static final MediaType mediaTypeForService(String service) {
        if (service == null) {
            return null;
        }
        if (RADIO_CHANNEL_MAP.containsKey(service)) {
            return MediaType.AUDIO;
        }
        if (TV_CHANNEL_MAP.containsKey(service)) {
            return MediaType.VIDEO;
        }
        return null;
    }

    public static final Specialization specializationForService(String service) {
        if (service == null) {
            return null;
        }
        if (RADIO_CHANNEL_MAP.containsKey(service)) {
            return Specialization.RADIO;
        }
        if (TV_CHANNEL_MAP.containsKey(service)) {
            return Specialization.TV;
        }
        return null;
    }
}
