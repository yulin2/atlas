package org.atlasapi.remotesite.youview;

import org.atlasapi.media.channel.Channel;

import com.google.common.base.Optional;

public interface YouViewChannelResolver {
    
    String getChannelUri(int channelId);
    
    Optional<Channel> getChannel(int channelId);
}
