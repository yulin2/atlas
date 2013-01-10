package org.atlasapi.remotesite.youview;

import java.util.Map;

import org.atlasapi.media.channel.Channel;

import com.google.common.base.Optional;

public class DummyYouViewChannelResolver implements YouViewChannelResolver {

    private final Map<Integer, Channel> channelMap;
    
    public DummyYouViewChannelResolver(Map<Integer, Channel> channelMap) {
        this.channelMap = channelMap;
    }
    
    @Override
    public String getChannelUri(int channelId) {
        if (channelMap.containsKey(channelId)) {
            return channelMap.get(channelId).uri();
        }
        return null;
    }

    @Override
    public Optional<Channel> getChannel(int channelId) {
        if (channelMap.containsKey(channelId)) {
            return Optional.fromNullable(channelMap.get(channelId));
        }
        return Optional.absent();
    }

}
