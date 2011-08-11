package org.atlasapi.remotesite.itv.interlinking;

import java.util.Map;

import org.atlasapi.media.entity.Channel;

import com.google.common.collect.Maps;

public class ItvInterlinkingChannelMap {
    private Map<String, Channel> idToChannel = Maps.newHashMap();

    public ItvInterlinkingChannelMap() {
        idToChannel.put("ITV1", Channel.ITV1_LONDON);
        idToChannel.put("ITV2", Channel.ITV2);
        idToChannel.put("ITV3", Channel.ITV3);
        idToChannel.put("ITV4", Channel.ITV4);
        idToChannel.put("CITV", Channel.CITV);
    }
    
    public Channel get(String key) {
        return idToChannel.get(key);
    }
}
