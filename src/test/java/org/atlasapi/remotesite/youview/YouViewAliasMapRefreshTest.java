package org.atlasapi.remotesite.youview;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.persistence.media.channel.ChannelStore;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.internal.ImmutableMap;
import com.google.inject.internal.ImmutableMap.Builder;
import com.metabroadcast.common.base.Maybe;

public class YouViewAliasMapRefreshTest {

    private static final DummyChannelStore channelStore = new DummyChannelStore();
    
    private static final YouViewChannelResolver youViewResolver = new DefaultYouViewChannelResolver(channelStore);
    
    @Test
    public void testAliasMappingRefresh() {
        Channel channel1 = new Channel(Publisher.METABROADCAST, "channel1", "key", true, MediaType.VIDEO, "someuri");
        channel1.addAliasUrl("nonYouViewAlias");
        Channel channel2 = new Channel(Publisher.METABROADCAST, "channel1", "key", true, MediaType.VIDEO, "someuri");
        channel2.addAliasUrl("http://youview.com/service/2");
        
        channelStore.createOrUpdate(channel1);
        channelStore.createOrUpdate(channel2);
        
        List<Channel> allChannels = youViewResolver.getAllChannels();
        assertEquals(ImmutableList.of(channel2), allChannels);
        
        channel1.addAliasUrl("http://youview.com/service/1");
        channelStore.createOrUpdate(channel1);
        
        allChannels = youViewResolver.getAllChannels();
        assertEquals(ImmutableList.of(channel1, channel2), allChannels);
    }

    private static class DummyChannelStore implements ChannelStore {

        private final Map<String, Channel> channelMapping = Maps.newHashMap();
        
        @Override
        @Deprecated
        public Maybe<Channel> fromKey(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Maybe<Channel> fromId(long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Maybe<Channel> fromUri(String uri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<Channel> forIds(Iterable<Long> ids) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<Channel> all() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Maybe<Channel> forAlias(String alias) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Channel> forAliases(String aliasPrefix) {
            Builder<String, Channel> results = ImmutableMap.builder(); 
            for (String alias : channelMapping.keySet()) {
                if (alias.contains(aliasPrefix)) {
                    results.put(alias, channelMapping.get(alias));
                }
            }
            return results.build();
        }

        @Override
        public Channel createOrUpdate(Channel channel) {
            for (String alias : channel.getAliasUrls()) {
                channelMapping.put(alias, channel);
            }
            return channel;
        }
        
    }
}
