package org.atlasapi.remotesite.bt.channels;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClientException;
import org.atlasapi.remotesite.bt.channels.mpxclient.Category;
import org.atlasapi.remotesite.bt.channels.mpxclient.Content;
import org.atlasapi.remotesite.bt.channels.mpxclient.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;


@RunWith( MockitoJUnitRunner.class )
public class WatchableChannelGroupSaverTest {
    
    private static final String CHANNEL1_ID = "kdcv";
    private static final String CHANNEL2_ID = "hqcs";
    
    private static final String ALIAS_URI_PREFIX = "http://example.org/";
    private static final String ALIAS_NAMESPACE = "a:namespace";
    
    private final ChannelGroupResolver channelGroupResolver = mock(ChannelGroupResolver.class);
    private final ChannelGroupWriter channelGroupWriter = mock(ChannelGroupWriter.class);
    private final ChannelResolver channelResolver = mock(ChannelResolver.class);
    private final ChannelWriter channelWriter = mock(ChannelWriter.class);
    
    private final NumberToShortStringCodec codec = SubstitutionTableNumberCodec.lowerCaseOnly();
    
    private final WatchableChannelGroupSaver saver 
        = new WatchableChannelGroupSaver(Publisher.METABROADCAST, ALIAS_URI_PREFIX, ALIAS_NAMESPACE, 
                channelGroupResolver, channelGroupWriter, channelResolver, channelWriter);
    
    
    @Test
    public void testsExtractsWatchableChannelGroup() throws BtMpxClientException {
        
        when(channelGroupResolver.channelGroupFor(ALIAS_URI_PREFIX + "watchables"))
            .thenReturn(Optional.<ChannelGroup>of(watchableChannelGroup()));
        
        saver.update(ImmutableList.of(watchableChannel(CHANNEL1_ID, true),
                watchableChannel(CHANNEL2_ID, false)));
        
        ArgumentCaptor<ChannelGroup> channelGroupCaptor = ArgumentCaptor.forClass(ChannelGroup.class);
        verify(channelGroupWriter).createOrUpdate(channelGroupCaptor.capture());

        ChannelGroup channelGroup = Iterables.getOnlyElement(channelGroupCaptor.getAllValues());
        
        assertThat(Iterables.getOnlyElement(channelGroup.getChannels()), is(codec.decode(CHANNEL1_ID).longValue()));
    }

    public Entry watchableChannel(String channelId, boolean isWatchable) {
        return new Entry(channelId, 0, "Title", 
                    ImmutableList.<Category>of(), 
                    ImmutableList.<Content>of(), 
                    true, null, null, isWatchable, false);
    }
    
    private ChannelGroup watchableChannelGroup() {
        ChannelGroup group = new Region();
        group.setCanonicalUri(ALIAS_URI_PREFIX + "watchables");
        group.setId(1L);
        return group;
    }
}
