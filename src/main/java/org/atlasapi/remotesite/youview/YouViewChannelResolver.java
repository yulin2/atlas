package org.atlasapi.remotesite.youview;

import java.util.Map;

import org.atlasapi.media.channel.Channel;

import com.google.common.base.Optional;

public interface YouViewChannelResolver {
    
    String getChannelUri(int channelId);
    
    Optional<Channel> getChannel(int channelId);
    
    Iterable<Channel> getAllChannels();
    
    Map<Integer, Channel> getAllChannelsByServiceId();

    String getChannelServiceAlias(int channelId);
}
