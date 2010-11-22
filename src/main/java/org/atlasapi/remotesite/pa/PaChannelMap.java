package org.atlasapi.remotesite.pa;

import java.util.Map;

import org.atlasapi.media.entity.Channel;

import com.google.common.collect.Maps;


public class PaChannelMap {

    Map<Integer, Channel> channelMap = Maps.newHashMap();
    
    public PaChannelMap() {
        channelMap.put(4, Channel.BBC_ONE);
        channelMap.put(1, Channel.BBC_ONE_NORTHERN_IRELAND);
        channelMap.put(2, Channel.BBC_ONE_SCOTLAND);
        channelMap.put(3, Channel.BBC_ONE_WALES);
        channelMap.put(9, Channel.BBC_ONE_EAST);
        channelMap.put(10, Channel.BBC_ONE_EAST_MIDLANDS);
//        channelMap.put(11, Channel.BBC_ONE_MIDLANDS);
//        channelMap.put(12, Channel.BBC_ONE_NORTH);
        channelMap.put(13, Channel.BBC_ONE_NORTH_EAST);
        channelMap.put(14, Channel.BBC_ONE_NORTH_WEST);
        channelMap.put(15, Channel.BBC_ONE_SOUTH);
        channelMap.put(16, Channel.BBC_ONE_SOUTH_EAST);
        channelMap.put(17, Channel.BBC_ONE_SOUTH_WEST);
        channelMap.put(18, Channel.BBC_ONE_WEST);
        channelMap.put(713, Channel.BBC_ONE_LONDON);
        
        channelMap.put(52, Channel.BBC_TWO);
        channelMap.put(19, Channel.BBC_TWO_NORTHERN_IRELAND);
        channelMap.put(20, Channel.BBC_TWO_SCOTLAND);
        channelMap.put(21, Channel.BBC_TWO_WALES);
//        channelMap.put(22, Channel.BBC_TWO_EAST);
//        channelMap.put(23, Channel.BBC_TWO_EAST_MIDLANDS);
//        channelMap.put(24, Channel.BBC_TWO_MIDLANDS);
//        channelMap.put(25, Channel.BBC_TWO_NORTH);
//        channelMap.put(26, Channel.BBC_TWO_NORTH_EAST);
//        channelMap.put(27, Channel.BBC_TWO_NORTH_WEST);
//        channelMap.put(29, Channel.BBC_TWO_SOUTH);
//        channelMap.put(30, Channel.BBC_TWO_SOUTH_EAST);
//        channelMap.put(31, Channel.BBC_TWO_SOUTH_WEST);
//        channelMap.put(32, Channel.BBC_TWO_WEST);
//        channelMap.put(724, Channel.BBC_TWO_LONDON);
    }
    
    public String getChannelUri(int channelId) {
        if (channelMap.containsKey(channelId)) {
            return channelMap.get(channelId).uri();
        }
        return null;
    }
    
}
