package org.atlasapi.remotesite.youview;

import static org.atlasapi.remotesite.pa.channels.PaChannelsIngester.YOUVIEW_SERVICE_ID_ALIAS_PREFIXES;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class DefaultYouViewChannelResolver implements YouViewChannelResolver {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultYouViewChannelResolver.class);
    
    private final Map<Integer, Channel> channelMap;
    private final Map<Integer, String> aliasMap;
    
    public DefaultYouViewChannelResolver(ChannelResolver channelResolver) {
        Builder<Integer, Channel> channelMapBuilder = ImmutableMap.builder();
        Builder<Integer, String> aliasMapBuilder = ImmutableMap.builder();
        
        for (String prefix : YOUVIEW_SERVICE_ID_ALIAS_PREFIXES) {
            buildEntriesForPrefix(channelResolver, prefix, channelMapBuilder, aliasMapBuilder);
        }
        
        channelMap = channelMapBuilder.build();
        aliasMap = aliasMapBuilder.build();
    }
    
    private void buildEntriesForPrefix(ChannelResolver channelResolver, 
            String prefix, Builder<Integer, Channel> channelMapBuilder,
            Builder<Integer, String> aliasMapBuilder) {
        
        Pattern pattern = Pattern.compile("^" + prefix + "(\\d+)$");
        for (Entry<String, Channel> entry: channelResolver.forAliases(prefix).entrySet()) {
            Matcher m = pattern.matcher(entry.getKey());
            if (m.matches()) {
                Integer channel = Integer.decode(m.group(1));
                
                channelMapBuilder.put(channel, entry.getValue());
                aliasMapBuilder.put(channel, entry.getKey());
            } else {
                log.error("Could not parse YouView alias " + entry.getKey());
            }
        }
    }

    @Override
    public String getChannelUri(int channelId) {
        if (channelMap.containsKey(channelId)) {
            return channelMap.get(channelId).getUri();
        }
        return null;
    }
    
    @Override
    public String getChannelServiceAlias(int channelId) {
        return aliasMap.get(channelId);
    }
    
    @Override
    public Optional<Channel> getChannel(int channelId) {
        if (channelMap.containsKey(channelId)) {
            return Optional.fromNullable(channelMap.get(channelId));
        }
        return Optional.absent();
    }

    @Override
    public Iterable<Channel> getAllChannels() {
        return channelMap.values();
    }

    @Override
    public Map<Integer, Channel> getAllChannelsByServiceId() {
        return channelMap;
    }

}
