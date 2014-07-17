package org.atlasapi.remotesite.bt.channels;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.v2.ChannelGroupController;
import org.atlasapi.remotesite.bt.channels.mpxclient.Category;
import org.atlasapi.remotesite.bt.channels.mpxclient.Entry;
import org.atlasapi.remotesite.bt.channels.mpxclient.Content;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;


@RunWith( MockitoJUnitRunner.class )
public class SubscriptionChannelGroupSaverTest {
    
    private static final String CHANNEL1_ID = "kdcv";
    private static final String CHANNEL2_ID = "hqcs";
    private static final String ALIAS_URI_PREFIX = "http://example.org/";
    private static final String ALIAS_NAMESPACE = "a:namespace";
    
    private final ChannelGroupResolver channelGroupResolver = mock(ChannelGroupResolver.class);
    private final ChannelGroupWriter channelGroupWriter = mock(ChannelGroupWriter.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ChannelWriter channelWriter = mock(ChannelWriter.class);
    
    private final NumberToShortStringCodec codec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final SubscriptionChannelGroupSaver saver 
        = new SubscriptionChannelGroupSaver(Publisher.METABROADCAST, ALIAS_URI_PREFIX, ALIAS_NAMESPACE, 
                channelGroupResolver, channelGroupWriter, channelResolver, channelWriter);
    
    
    @Test
    public void testExtractsSubscriptions() {
        String theirId1 = "S0123456";
        String theirId2 = "S6543210";
        
        when(channelGroupResolver.channelGroupFor(ALIAS_URI_PREFIX + theirId1))
            .thenReturn(Optional.<ChannelGroup>of(channelGroup(theirId1, 1)));
        when(channelGroupResolver.channelGroupFor(ALIAS_URI_PREFIX + theirId2))
            .thenReturn(Optional.<ChannelGroup>of(channelGroup(theirId2, 2)));
        saver.update(ImmutableList.of(channelWithSubscription(CHANNEL1_ID, "S0123456"),
                channelWithSubscription(CHANNEL2_ID, "S6543210")));
        
        ArgumentCaptor<ChannelGroup> channelGroupCaptor = ArgumentCaptor.forClass(ChannelGroup.class);
        verify(channelGroupWriter, times(2)).createOrUpdate(channelGroupCaptor.capture());

        Map<String, ChannelGroup> map = Maps.uniqueIndex(channelGroupCaptor.getAllValues(), new Function<ChannelGroup, String>() {

            @Override
            public String apply(ChannelGroup input) {
                return Iterables.getOnlyElement(input.getAliases()).getValue();
            }
            
        });
        assertThat(Iterables.getOnlyElement(map.get("S0123456").getChannels()), is(codec.decode(CHANNEL1_ID).longValue()));
    }
    
    public Entry channelWithSubscription(String channelId, String subscriptionId) {
        Category category = new Category(subscriptionId, "subscription", "");
        return new Entry(channelId, 0, "Title", 
                    ImmutableList.of(category), 
                    ImmutableList.<Content>of(), 
                    true, null, null, true, false);
    }
    
    private ChannelGroup channelGroup(String remoteId, long atlasId) {
        ChannelGroup group = new Region();
        group.setCanonicalUri(ALIAS_URI_PREFIX + remoteId);
        group.setId(atlasId);
        group.setAliases(ImmutableSet.of(new Alias(ALIAS_NAMESPACE, remoteId)));
        return group;
    }
}
