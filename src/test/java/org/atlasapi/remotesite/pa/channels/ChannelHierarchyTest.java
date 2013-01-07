package org.atlasapi.remotesite.pa.channels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.pa.channels.bindings.Channels;
import org.atlasapi.remotesite.pa.channels.bindings.Logo;
import org.atlasapi.remotesite.pa.channels.bindings.Logos;
import org.atlasapi.remotesite.pa.channels.bindings.Name;
import org.atlasapi.remotesite.pa.channels.bindings.Names;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class ChannelHierarchyTest {

    private final PaChannelsProcessor processor = new PaChannelsProcessor(Mockito.mock(ChannelResolver.class), Mockito.mock(ChannelWriter.class), Mockito.mock(ChannelGroupResolver.class), Mockito.mock(ChannelGroupWriter.class));
    
    @Test
    public void testStationWithSingleChannel() {
        ChannelInfo channel = new ChannelInfo();
        channel.setId("1741");
        channel.setName("Heat TV");
        channel.setImage("p131906.png");
        
        Channel createdChannel = processor.processStandaloneChannel(channel.createPaChannel());
        
        assertEquals("http://ref.atlasapi.org/channels/heattv", createdChannel.getCanonicalUri());
        assertEquals("http://pressassociation.com/channels/1741", Iterables.getOnlyElement(createdChannel.getAliases()));
        assertEquals("Heat TV", createdChannel.title());
        assertEquals("http://images.atlas.metabroadcast.com/pressassociation.com/channels/p131906.png", createdChannel.image());
        assertEquals(Publisher.METABROADCAST, createdChannel.source());
    }
    
    @Test
    public void testStationWithMultipleChannels() {
        ChannelInfo westMidlands = new ChannelInfo();
        westMidlands.setId("11");
        westMidlands.setName("BBC One West Midlands");
        westMidlands.setImage("p131474.png");
        
        ChannelInfo channelIslands = new ChannelInfo();
        channelIslands.setId("1663");
        channelIslands.setName("BBC One Channel Islands");
        channelIslands.setImage("p131731.png");
        
        StationInfo station = new StationInfo();
        station.setId("1");
        station.setName("BBC One");
//        station.setImage("");
        station.addChannel(westMidlands);
        station.addChannel(channelIslands);
        
        Channel parent = processor.processParentChannel(station.createPaStation());
        List<Channel> children = processor.processChildChannels(station.createPaStation().getChannels().getChannel());
        
        assertEquals(Publisher.METABROADCAST, parent.source());
        assertEquals("http://ref.atlasapi.org/channels/bbcone", parent.uri());
        assertEquals("BBC One", parent.title());
//      assertEquals("", parent.image());
        assertEquals("http://pressassociation.com/channels/1", Iterables.getOnlyElement(parent.getAliases()));
        
        Channel westMids = Channel.builder()
                .withSource(Publisher.METABROADCAST)
                .withUri("http://ref.atlasapi.org/channels/bbconewestmidlands")
                .withTitle("BBC One West Midlands")
                .withImage("http://images.atlas.metabroadcast.com/pressassociation.com/channels/p131474.png")
                .build();
        westMids.addAlias("http://pressassociation.com/channels/11");
        
        Channel channelIsl = Channel.builder()
                .withSource(Publisher.METABROADCAST)
                .withUri("http://ref.atlasapi.org/channels/bbconechannelislands")
                .withTitle("BBC One Channel Islands")
                .withImage("http://images.atlas.metabroadcast.com/pressassociation.com/channels/p131731.png")
                .build();
        channelIsl.addAlias("http://pressassociation.com/channels/1663");
        
        ExtendedChannelEquivalence equiv = new ExtendedChannelEquivalence();
        assertTrue(equiv.pairwise().equivalent(ImmutableList.of(westMids, channelIsl), ImmutableList.copyOf(children)));
    }
    
    private class ExtendedChannelEquivalence extends Equivalence<Channel> {

        @Override
        protected boolean doEquivalent(Channel a, Channel b) {
            return a.source().equals(b.source())
                    && a.uri().equals(b.uri())
                    && a.title().equals(b.title())
                    // will test these fields once they're in the output from PA and can be ingested
//                    && a.image().equals(b.image())
//                    && a.mediaType().equals(b.mediaType())
//                    && a.highDefinition().equals(b.highDefinition())
                    && a.variations().equals(b.variations())
                    && ((a.parent() == null && b.parent() == null) || a.parent().equals(b.parent()));
        }

        @Override
        protected int doHash(Channel t) {
            // TODO Auto-generated method stub
            return 0;
        }
        
    }
    
    private class ChannelInfo {
        private String name;
        private String id;
        private String image;
        
        public void setName(String name) {
            this.name = name;
        }
        public void setId(String id) {
            this.id = id;
        }
        public void setImage(String image) {
            this.image = image;
        }
        
        public org.atlasapi.remotesite.pa.channels.bindings.Channel createPaChannel() {
            org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel = new org.atlasapi.remotesite.pa.channels.bindings.Channel();
            
            Names paChannelNames = new Names();
            Name paChannelName = new Name();
            paChannelName.setvalue(name);
            paChannelNames.getName().add(paChannelName);
            paChannel.setNames(paChannelNames);
            
            Logos logos = new Logos();
            Logo logo = new Logo();
            logo.setvalue(image);
            logos.getLogo().add(logo);
            paChannel.setLogos(logos);
            
            paChannel.setId(id);
            
            return paChannel;
        }
    }
    
    private class StationInfo {
        private String name;
        private String id;
        private String image;
        private List<ChannelInfo> channels = Lists.newArrayList();
        
        public void setName(String name) {
            this.name = name;
        }
        public void setId(String id) {
            this.id = id;
        }
        public void setImage(String image) {
            this.image = image;
        }
        
        public void addChannel(ChannelInfo channel) {
            channels.add(channel);
        }
        
        public org.atlasapi.remotesite.pa.channels.bindings.Station createPaStation() {
            org.atlasapi.remotesite.pa.channels.bindings.Station paStation = new org.atlasapi.remotesite.pa.channels.bindings.Station();
            
            Names paChannelNames = new Names();
            Name paChannelName = new Name();
            paChannelName.setvalue(name);
            paChannelNames.getName().add(paChannelName);
            paStation.setNames(paChannelNames);
            
            // can't set station image currently
//            Logos logos = new Logos();
//            Logo logo = new Logo();
//            logo.setvalue(image);
//            logos.getLogo().add(logo);
//            paStation.setLogos(logos);
            
            paStation.setId(id);
            
            Channels paChannels = new Channels();
            for (ChannelInfo channel : channels) {
                paChannels.getChannel().add(channel.createPaChannel());
            }
            
            paStation.setChannels(paChannels);
            
            return paStation;
        }
    }
}
