package org.atlasapi.remotesite.bt.channels;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClient;
import org.atlasapi.remotesite.bt.channels.mpxclient.BtMpxClientException;
import org.atlasapi.remotesite.bt.channels.mpxclient.Category;
import org.atlasapi.remotesite.bt.channels.mpxclient.Content;
import org.atlasapi.remotesite.bt.channels.mpxclient.Entry;
import org.atlasapi.remotesite.bt.channels.mpxclient.PaginatedEntries;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.query.Selection;


@RunWith( MockitoJUnitRunner.class )
public class TargetUserGroupChannelSaverTest {
    
    private static final String TARGET_USER_GROUP_B = "gbr-B";
    private static final String TARGET_USER_GROUP_A = "gbr-A";
    private static final String CHANNEL1_ID = "kdcv";
    private static final String CHANNEL2_ID = "hqcs";
     
    private static final Map<String, String> targetUserGroupKeyToUri = 
            ImmutableMap.of(
                    TARGET_USER_GROUP_A, "http://example.org/a", 
                    TARGET_USER_GROUP_B, "http://example.org/b");
    
    private static final String ALIAS_URI_PREFIX = "http://example.org/";
    private static final String ALIAS_NAMESPACE = "a:namespace";
    
    private final ChannelGroupResolver channelGroupResolver = mock(ChannelGroupResolver.class);
    private final ChannelGroupWriter channelGroupWriter = mock(ChannelGroupWriter.class);
    private final BtMpxClient btMpxClient = mock(BtMpxClient.class);
    private final NumberToShortStringCodec codec = SubstitutionTableNumberCodec.lowerCaseOnly();
    
    private final TargetUserGroupChannelGroupSaver saver 
        = new TargetUserGroupChannelGroupSaver(Publisher.METABROADCAST, ALIAS_URI_PREFIX, ALIAS_NAMESPACE, 
                channelGroupResolver, channelGroupWriter, btMpxClient);
    
    
    @Test
    public void testExtractsTargetUserGroups() throws BtMpxClientException {
        
        when(channelGroupResolver.fromAlias(canonicalUriFor(TARGET_USER_GROUP_A)))
            .thenReturn(Optional.<ChannelGroup>of(channelGroup(TARGET_USER_GROUP_A, 1)));
        when(channelGroupResolver.fromAlias(canonicalUriFor(TARGET_USER_GROUP_B)))
            .thenReturn(Optional.<ChannelGroup>of(channelGroup(TARGET_USER_GROUP_B, 2)));
        when(btMpxClient.getCategories(Optional.<Selection>absent()))
            .thenReturn(categoryLookups());
        saver.update(ImmutableList.of(channelWithTargetUserGroup(CHANNEL1_ID, TARGET_USER_GROUP_A),
                channelWithTargetUserGroup(CHANNEL2_ID, TARGET_USER_GROUP_B)));
        
        ArgumentCaptor<ChannelGroup> channelGroupCaptor = ArgumentCaptor.forClass(ChannelGroup.class);
        verify(channelGroupWriter, times(2)).createOrUpdate(channelGroupCaptor.capture());

        Map<String, ChannelGroup> map = Maps.uniqueIndex(channelGroupCaptor.getAllValues(), new Function<ChannelGroup, String>() {

            @Override
            public String apply(ChannelGroup input) {
                return Iterables.getOnlyElement(input.getAliases()).getValue();
            }
            
        });
        
        assertThat(Iterables.getOnlyElement(map.get(TARGET_USER_GROUP_A).getChannels()), is(codec.decode(CHANNEL1_ID).longValue()));
        assertThat(Iterables.getOnlyElement(map.get(TARGET_USER_GROUP_B).getChannels()), is(codec.decode(CHANNEL2_ID).longValue()));
    }
    
    private PaginatedEntries categoryLookups() {
        Builder<Entry> entries = ImmutableList.builder();
        for (Map.Entry<String, String> entry : targetUserGroupKeyToUri.entrySet()) {
            entries.add(new Entry(entry.getValue(), 0, "Title", ImmutableList.<Category>of(), 
                    ImmutableList.<Content>of(), true, entry.getKey(), null, true, false));
        }
        return new PaginatedEntries(0, 100, 2, "", entries.build());
    }

    public Entry channelWithTargetUserGroup(String channelId, String label) {
        //{"name":"S0128048","scheme":"subscription","label":""},{"name":"Linear TUG - GBR-bt_multicast","scheme":"targetUserGroup","label":"GBR-bt_multicast"},{"name":"Linear TUG - GBR-bt_broadband","scheme":"targetUserGroup","label":"GBR-bt_broadband"}
        Category category = new Category("Linear TUG", "targetUserGroup", label);
        return new Entry(channelId, 0, "Title", 
                    ImmutableList.of(category), 
                    ImmutableList.<Content>of(), 
                    true, null, null, true, false);
    }
    
    private String canonicalUriFor(String key) {
        return ALIAS_URI_PREFIX + targetUserGroupKeyToUri.get(key).replace("http://", "");
    }
    
    private ChannelGroup channelGroup(String key, long atlasId) {
        ChannelGroup group = new Region();
        group.setCanonicalUri(canonicalUriFor(key));
        group.setId(atlasId);
        group.setAliases(ImmutableSet.of(new Alias(ALIAS_NAMESPACE, key)));
        return group;
    }
}
