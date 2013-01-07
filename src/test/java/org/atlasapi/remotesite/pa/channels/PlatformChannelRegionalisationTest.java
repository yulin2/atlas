package org.atlasapi.remotesite.pa.channels;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class PlatformChannelRegionalisationTest {

    private PaChannelsProcessor processor;
    
    @Test
    public void testPlatformChannelsNoRegionalisation() {
        EpgContentInfo epgContent = new EpgContentInfo();
        epgContent.setChannelId("1404");
        epgContent.setChannelNumber("104");
        
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setName("Freeview");
        platformInfo.setId("3");
        platformInfo.setEpgContents(Lists.newArrayList(epgContent));
        
        // create serviceProvider with no regions
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setRegions(Lists.<RegionalisationInfo>newArrayList());
        
        // add correct channel and an incorrect channel
        List<Channel> channels = Lists.newArrayList();
        Channel channel4HD = new Channel(Publisher.METABROADCAST, "Channel 4 HD", "channel4hd", true, MediaType.VIDEO, "http://ref.atlasapi.org/channels/channel4hd");
        channel4HD.setId(1234L);
        channel4HD.addAlias("http://pressassociation.com/channels/1404");
        channels.add(channel4HD);
        Channel HeatTV = new Channel(Publisher.METABROADCAST, "Heat TV", "heattv", false, MediaType.VIDEO, "http://ref.atlasapi.org/channels/heattv");
        HeatTV.addAlias("http://pressassociation.com/channels/1741");
        channels.add(HeatTV);
        
        ChannelResolver channelResolver = new DummyChannelResolver(channels);
        
        processor = new PaChannelsProcessor(channelResolver, Mockito.mock(ChannelWriter.class), Mockito.mock(ChannelGroupResolver.class), Mockito.mock(ChannelGroupWriter.class));
        
        List<ChannelGroup> channelGroups = processor.processPlatform(platformInfo.createPlatform(), serviceProvider.createServiceProvider(), Lists.<org.atlasapi.remotesite.pa.channels.bindings.Region>newArrayList());
        
        ChannelGroup result = Iterables.getOnlyElement(channelGroups);
        Platform platform = (Platform)result;
        
        // check there are no regions
        assertTrue(platform.getRegions().isEmpty());
        
        ChannelNumbering channelNumbering = Iterables.getOnlyElement(platform.getChannelNumberings());
        
        assertThat(channelNumbering.getChannelNumber(), is(104));
        
        assertEquals(new Long(1234L), channelNumbering.getChannel());
        
        assertEquals(ImmutableList.of(channelNumbering), ImmutableList.copyOf(channel4HD.channelNumbers()));
    }

    @Test
    public void testPlatformChannelsRegionalisation() {
        RegionalisationInfo epgRegion = new RegionalisationInfo();
        epgRegion.setRegionId("61");
        
        EpgContentInfo epgContent = new EpgContentInfo();
        epgContent.setChannelId("15");
        epgContent.setChannelNumber("1");
        epgContent.setRegions(Lists.newArrayList(epgRegion));
        
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setName("Freeview");
        platformInfo.setId("3");
        platformInfo.setEpgContents(Lists.newArrayList(epgContent));
        
        // create serviceProvider with a region
        RegionalisationInfo serviceProviderRegion = new RegionalisationInfo();
        serviceProviderRegion.setRegionId("61");
        
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setRegions(Lists.newArrayList(serviceProviderRegion));
        
        RegionInfo regionInfo = new RegionInfo();
        regionInfo.setId("61");
        regionInfo.setName("South");
        
        // add correct channel and an incorrect channel
        List<Channel> channels = Lists.newArrayList();
        Channel bbcOneSouth = new Channel(Publisher.METABROADCAST, "BBC One South", "south", false, MediaType.VIDEO, "http://www.bbc.co.uk/services/bbcone/south");
        bbcOneSouth.setId(1234L);
        bbcOneSouth.addAlias("http://pressassociation.com/channels/15");
        channels.add(bbcOneSouth);
        
        ChannelResolver channelResolver = new DummyChannelResolver(channels);
        
        processor = new PaChannelsProcessor(channelResolver, Mockito.mock(ChannelWriter.class), Mockito.mock(ChannelGroupResolver.class), Mockito.mock(ChannelGroupWriter.class));
        
        List<ChannelGroup> channelGroups = processor.processPlatform(platformInfo.createPlatform(), serviceProvider.createServiceProvider(), Lists.newArrayList(regionInfo.createRegion()));
        
        assertThat(channelGroups.size(), is(2));
        
        Platform platform = null;
        Region region = null;
        
        for (ChannelGroup result : channelGroups) {
            if (result instanceof Platform) {
                platform = (Platform) result;
            }
            if (result instanceof Region) {
                region = (Region) result;
            }
        }

        // check that a platform and a region have been found
        assertTrue(platform != null);
        assertTrue(region != null);
        
        assertEquals(region, Iterables.getOnlyElement(platform.getRegions()));
        assertEquals(platform, region.getPlatform());
        
        assertTrue(platform.getChannelNumberings().isEmpty());
        
        ChannelNumbering channelNumbering = Iterables.getOnlyElement(region.getChannelNumberings());
        
        assertThat(channelNumbering.getChannelNumber(), is(1));
        
        assertEquals(new Long(1234L), channelNumbering.getChannel());
        
        assertEquals(ImmutableList.of(channelNumbering), ImmutableList.copyOf(bbcOneSouth.channelNumbers()));
    }
    
    @Test
    public void testPlatformChannelsPartialRegionalisation() {
        RegionalisationInfo epgRegion = new RegionalisationInfo();
        epgRegion.setRegionId("61");
        
        EpgContentInfo epgContent1 = new EpgContentInfo();
        epgContent1.setChannelId("15");
        epgContent1.setChannelNumber("1");
        epgContent1.setRegions(Lists.newArrayList(epgRegion));
        
        EpgContentInfo epgContent2 = new EpgContentInfo();
        epgContent2.setChannelId("1499");
        epgContent2.setChannelNumber("34");
        
        RegionalisationInfo epgRegion2 = new RegionalisationInfo();
        epgRegion2.setRegionId("58");
        
        EpgContentInfo epgContent3 = new EpgContentInfo();
        epgContent3.setChannelId("413");
        epgContent3.setChannelNumber("1");
        epgContent3.setRegions(Lists.newArrayList(epgRegion2));
        
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setName("Freeview");
        platformInfo.setId("3");
        platformInfo.setEpgContents(Lists.newArrayList(epgContent1, epgContent2));
        
        // create serviceProvider with a region
        RegionalisationInfo serviceProviderRegion = new RegionalisationInfo();
        serviceProviderRegion.setRegionId("61");
        
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setRegions(Lists.newArrayList(serviceProviderRegion));
        
        RegionInfo regionInfo = new RegionInfo();
        regionInfo.setId("61");
        regionInfo.setName("South");
        
        // add correct channel and an incorrect channel
        List<Channel> channels = Lists.newArrayList();
        Channel bbcOneSouth = new Channel(Publisher.METABROADCAST, "BBC One South", "south", false, MediaType.VIDEO, "http://www.bbc.co.uk/services/bbcone/south");
        bbcOneSouth.setId(1234L);
        bbcOneSouth.addAlias("http://pressassociation.com/channels/15");
        Channel topUpTV = new Channel(Publisher.METABROADCAST, "ESPN Freeview/TopUp TV", "espnfreeview", true, MediaType.VIDEO, "http://ref.atlasapi.org/channels/espnfreeview");
        topUpTV.setId(2345L);
        topUpTV.addAlias("http://pressassociation.com/channels/1499");
        channels.add(bbcOneSouth);
        channels.add(topUpTV);
        
        ChannelResolver channelResolver = new DummyChannelResolver(channels);
        
        processor = new PaChannelsProcessor(channelResolver, Mockito.mock(ChannelWriter.class), Mockito.mock(ChannelGroupResolver.class), Mockito.mock(ChannelGroupWriter.class));
        
        List<ChannelGroup> channelGroups = processor.processPlatform(platformInfo.createPlatform(), serviceProvider.createServiceProvider(), Lists.newArrayList(regionInfo.createRegion()));
        
        assertThat(channelGroups.size(), is(2));
        
        Platform platform = null;
        Region region = null;
        
        for (ChannelGroup result : channelGroups) {
            if (result instanceof Platform) {
                platform = (Platform) result;
            }
            if (result instanceof Region) {
                region = (Region) result;
            }
        }

        // check that a platform and a region have been found
        assertTrue(platform != null);
        assertTrue(region != null);
        
        assertEquals(region, Iterables.getOnlyElement(platform.getRegions()));
        assertEquals(platform, region.getPlatform());
        
        assertTrue(platform.getChannelNumberings().isEmpty());
        
        // check that list of channels on region matches the region channel and the non-regionalised channel
        assertEquals(ImmutableList.of(1234L, 2345L), ImmutableList.copyOf(Iterables.transform(region.getChannelNumberings(), new Function<ChannelNumbering, Long>() {
            @Override
            public Long apply(ChannelNumbering input) {
                return input.getChannel();
            }
        })));
        
        ChannelNumbering regionalChannelNumbering = Iterables.getOnlyElement(bbcOneSouth.channelNumbers());
        ChannelNumbering platformChannelNumbering = Iterables.getOnlyElement(topUpTV.channelNumbers());
        
        assertThat(regionalChannelNumbering.getChannelNumber(), is(1));
        assertThat(platformChannelNumbering.getChannelNumber(), is(34));
        
        assertEquals(new Long(1234L), regionalChannelNumbering.getChannel());
        assertEquals(new Long(2345L), platformChannelNumbering.getChannel());
        
        assertEquals(ImmutableList.of(platformChannelNumbering), ImmutableList.copyOf(topUpTV.channelNumbers()));
        assertEquals(ImmutableList.of(regionalChannelNumbering), ImmutableList.copyOf(bbcOneSouth.channelNumbers()));
    }        
}

class DummyChannelResolver implements ChannelResolver {

    private List<Channel> channels;
    
    public DummyChannelResolver(Iterable<Channel> channels) {
         this.channels = ImmutableList.copyOf(channels);
    }
    
    @Override
    public Maybe<Channel> fromKey(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Maybe<Channel> fromId(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Maybe<Channel> fromUri(String uri) {
        for (Channel channel : channels) {
            if (channel.getCanonicalUri().equals(uri)) {
                return Maybe.just(channel);
            }
        }
        return Maybe.nothing();
    }

    @Override
    public Collection<Channel> all() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Channel> forAliases(String aliasPrefix) {
        final Pattern prefixPattern = Pattern.compile(String.format("^%s", Pattern.quote(aliasPrefix)));

        Builder<String, Channel> channelMap = ImmutableMap.builder();
        for (Channel channel : channels) {
            for (String alias : Iterables.filter(channel.getAliases(), Predicates.contains(prefixPattern))) {
                channelMap.put(alias, channel);
            }
        }
        return channelMap.build();
    }

    @Override
    public Iterable<Channel> forIds(Iterable<Long> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Maybe<Channel> forAlias(String alias) {
        for (Channel channel : channels) {
            for (String channelAlias : channel.getAliases()) {
                if (channelAlias.equals(alias)) {
                    return Maybe.just(channel);
                }
            }
        }
        return Maybe.nothing();
    }
    
}
