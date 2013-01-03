package org.atlasapi.remotesite.pa.channels;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.pa.channels.bindings.Epg;
import org.atlasapi.remotesite.pa.channels.bindings.Name;
import org.atlasapi.remotesite.pa.channels.bindings.Names;
import org.atlasapi.remotesite.pa.channels.bindings.Regionalisation;
import org.atlasapi.remotesite.pa.channels.bindings.RegionalisationList;
import org.atlasapi.remotesite.pa.channels.bindings.ServiceProvider;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PlatformRegionalisationTest {

    private final PaChannelsProcessor processor = new PaChannelsProcessor(Mockito.mock(ChannelResolver.class), Mockito.mock(ChannelWriter.class), Mockito.mock(ChannelGroupResolver.class), Mockito.mock(ChannelGroupWriter.class));
    
    @Test
    public void testNonRegionalisedPlatform() {
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setName("Freeview");
        platformInfo.setId("3");
        // create serviceProvider with no regions
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setRegions(Lists.<RegionalisationInfo>newArrayList());
        // create region
        org.atlasapi.remotesite.pa.channels.bindings.Region region = Mockito.mock(org.atlasapi.remotesite.pa.channels.bindings.Region.class);
        
        List<ChannelGroup> channelGroups = processor.processPlatform(platformInfo.createPlatform(), serviceProvider.createServiceProvider(), Lists.newArrayList(region));

        // assert no regions on platform
        ChannelGroup result = Iterables.getOnlyElement(channelGroups);
        Platform platform = (Platform) result;
        
        assertTrue(platform.getRegions().isEmpty());
        // test that platform fields are picked up ok
        assertEquals("Freeview", platform.getTitle());
        assertEquals("http://pressassociation.com/platforms/3", Iterables.getOnlyElement(platform.getAliases()));
        assertEquals(Publisher.METABROADCAST, platform.getPublisher());
    }
    
    @Test
    public void testRegionalisedPlatform() {
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setName("Freeview");
        platformInfo.setId("3");
        // create serviceProvider with a region
        RegionalisationInfo regionalisation = new RegionalisationInfo();
        regionalisation.setRegionId("61");
        
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setRegions(Lists.newArrayList(regionalisation));
        
        // create region
        RegionInfo regionInfo = new RegionInfo();
        regionInfo.setId("61");
        regionInfo.setName("South");
        
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
        
        assertEquals("South", region.getTitle());
        assertEquals("http://pressassociation.com/regions/61", Iterables.getOnlyElement(region.getAliases()));
        assertEquals(Publisher.METABROADCAST, region.getPublisher());
        assertEquals(platform, region.getPlatform());
        
        // test that platform fields are picked up ok
        assertEquals("Freeview", platform.getTitle());
        assertEquals("http://pressassociation.com/platforms/3", Iterables.getOnlyElement(platform.getAliases()));
        assertEquals(Publisher.METABROADCAST, platform.getPublisher());
        
        Region nestedRegion = Iterables.getOnlyElement(platform.getRegions()); 
        assertEquals(region, nestedRegion);        
    }
    
    private class PlatformInfo {
        private String name;
        private String id;
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public org.atlasapi.remotesite.pa.channels.bindings.Platform createPlatform() {
            org.atlasapi.remotesite.pa.channels.bindings.Platform platform = new org.atlasapi.remotesite.pa.channels.bindings.Platform();
            
            Names paPlatformNames = new Names();
            Name paPlatformName = new Name();
            paPlatformName.setvalue(name);
            paPlatformNames.getName().add(paPlatformName);
            platform.setNames(paPlatformNames);
            
            Epg epg = new Epg();
            platform.setEpg(epg);
            
            platform.setId(id);
            
            return platform;
        }
    }
    
    private class ServiceProviderInfo {
        private List<RegionalisationInfo> regions;

        public void setRegions(List<RegionalisationInfo> regions) {
            this.regions = regions;
        }
        
        public ServiceProvider createServiceProvider() {
            ServiceProvider serviceProvider = new ServiceProvider();
            
            RegionalisationList regionalisationList = new RegionalisationList();
            for (RegionalisationInfo regionalisation : regions) {
                regionalisationList.getRegionalisation().add(regionalisation.createRegionalisation());                
            }
            serviceProvider.setRegionalisationList(regionalisationList);
            
            return serviceProvider;
        }
    }
    
    private class RegionalisationInfo {
        private String regionId;
        
        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }
        
        public Regionalisation createRegionalisation() {
            Regionalisation regionalisation = new Regionalisation();
            
            regionalisation.setRegionId(regionId);
            
            return regionalisation;
        }
    }
    
    private class RegionInfo {
        private String name;
        private String id;
        
        public void setId(String id) {
            this.id = id;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public org.atlasapi.remotesite.pa.channels.bindings.Region createRegion() {
            org.atlasapi.remotesite.pa.channels.bindings.Region region = new org.atlasapi.remotesite.pa.channels.bindings.Region();
            
            Names paRegionNames = new Names();
            Name paRegionName = new Name();
            paRegionName.setvalue(name);
            paRegionNames.getName().add(paRegionName);
            region.setNames(paRegionNames);
            
            region.setId(id);
            
            return region;
        }
    }
}
