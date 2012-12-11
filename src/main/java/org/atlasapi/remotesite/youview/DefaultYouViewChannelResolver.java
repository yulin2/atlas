package org.atlasapi.remotesite.youview;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.persistence.media.channel.ChannelResolver;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class DefaultYouViewChannelResolver implements YouViewChannelResolver {
    
    private static final String YOUVIEW_URI_PREFIX = "http://youview.com/service/";
    private static Pattern YOUVIEW_URI_MATCHER = Pattern.compile("^" + YOUVIEW_URI_PREFIX + "(\\d+)$");
    
    private final Map<Integer, Channel> channelMap = Maps.newHashMap();
    
    public DefaultYouViewChannelResolver(ChannelResolver channelResolver) {
        for (Entry<String, Channel> entry: channelResolver.forAliases(YOUVIEW_URI_PREFIX).entrySet()) {
            Matcher m = YOUVIEW_URI_MATCHER.matcher(entry.getKey());
            if(m.matches()) {
                Integer channel = Integer.decode(m.group(1));
                channelMap.put(channel, entry.getValue());
            }
        }
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
