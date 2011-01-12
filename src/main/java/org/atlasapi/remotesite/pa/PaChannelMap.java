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
        
        channelMap.put(935, Channel.BBC_THREE);
        channelMap.put(742, Channel.BBC_FOUR);
        channelMap.put(1228, Channel.BBC_HD);
        
        channelMap.put(49, Channel.ITV1_LONDON);
        channelMap.put(35, Channel.ITV1_GRANADA);
        channelMap.put(37, Channel.ITV1_TYNE_TEES);
        channelMap.put(38, Channel.ITV1_BORDER_SOUTH);
        channelMap.put(42, Channel.ITV1_MERIDIAN);
        channelMap.put(43, Channel.ITV1_ANGLIA);
        channelMap.put(44, Channel.ITV1_CHANNEL);
        channelMap.put(45, Channel.ITV1_WALES);
        channelMap.put(46, Channel.ITV1_WEST);
        channelMap.put(47, Channel.ITV1_CARLTON_CENTRAL);
        channelMap.put(48, Channel.ITV1_CARLTON_WESTCOUNTRY);
        channelMap.put(698, Channel.ITV1_BORDER_NORTH);
        channelMap.put(1296, Channel.ITV1_THAMES_VALLEY_NORTH);
        channelMap.put(1297, Channel.ITV1_THAMES_VALLEY_SOUTH);
        channelMap.put(1560, Channel.ITV1_HD);
        channelMap.put(47, Channel.ITV1_CARLTON_CENTRAL);
        
        channelMap.put(451, Channel.ITV2);
        channelMap.put(1602, Channel.ITV2_HD);
        channelMap.put(1065, Channel.ITV3);
        channelMap.put(1612, Channel.ITV3_HD);
        channelMap.put(1174, Channel.ITV4);
        channelMap.put(1613, Channel.ITV4_HD);
        
        channelMap.put(53, Channel.CHANNEL_FOUR);
        channelMap.put(605, Channel.E_FOUR);
        channelMap.put(1167, Channel.MORE_FOUR);
        
        channelMap.put(54, Channel.FIVE);
        channelMap.put(1289, Channel.FIVER);
        channelMap.put(1290, Channel.FIVE_USA);
    }
    
    public String getChannelUri(int channelId) {
        if (channelMap.containsKey(channelId)) {
            return channelMap.get(channelId).uri();
        }
        return null;
    }
    
}
