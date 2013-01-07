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
import org.atlasapi.remotesite.pa.channels.bindings.EpgContent;
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

        List<ChannelGroup> channelGroups = processor.processPlatform(platformInfo.createPlatform(), serviceProvider.createServiceProvider(), Lists.<org.atlasapi.remotesite.pa.channels.bindings.Region>newArrayList());

        // assert no regions on platform
        ChannelGroup result = Iterables.getOnlyElement(channelGroups);
        Platform platform = (Platform) result;
        
        assertTrue(platform.getRegions().isEmpty());
        // test that platform fields are picked up ok
        assertEquals("Freeview", platform.getTitle());
        assertEquals("http://ref.atlasapi.org/platforms/freeview", platform.getCanonicalUri());
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
        
        // create regions
        RegionInfo south = new RegionInfo();
        south.setId("61");
        south.setName("South");
        
        RegionInfo yorkshire = new RegionInfo();
        yorkshire.setId("67");
        yorkshire.setName("Yorkshire");
        
        List<ChannelGroup> channelGroups = processor.processPlatform(platformInfo.createPlatform(), serviceProvider.createServiceProvider(), Lists.newArrayList(south.createRegion(), yorkshire.createRegion()));

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
        assertEquals("http://ref.atlasapi.org/regions/south", region.getCanonicalUri());
        assertEquals("http://pressassociation.com/regions/61", Iterables.getOnlyElement(region.getAliases()));
        assertEquals(Publisher.METABROADCAST, region.getPublisher());
        assertEquals(platform.getId(), region.getPlatform());
        
        // test that platform fields are picked up ok
        assertEquals("Freeview", platform.getTitle());
        assertEquals("http://ref.atlasapi.org/platforms/freeview", platform.getCanonicalUri());
        assertEquals("http://pressassociation.com/platforms/3", Iterables.getOnlyElement(platform.getAliases()));
        assertEquals(Publisher.METABROADCAST, platform.getPublisher());
        
        Long nestedRegionId = Iterables.getOnlyElement(platform.getRegions()); 
        assertEquals(region.getId(), nestedRegionId);        
    }
}

class PlatformInfo {
    private String name;
    private String id;
    private List<EpgContentInfo> epgContents = Lists.newArrayList();
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setEpgContents(List<EpgContentInfo> epgContents) {
        this.epgContents = epgContents;
    }
    
    public org.atlasapi.remotesite.pa.channels.bindings.Platform createPlatform() {
        org.atlasapi.remotesite.pa.channels.bindings.Platform platform = new org.atlasapi.remotesite.pa.channels.bindings.Platform();
        
        Names paPlatformNames = new Names();
        Name paPlatformName = new Name();
        paPlatformName.setvalue(name);
        paPlatformNames.getName().add(paPlatformName);
        platform.setNames(paPlatformNames);
        
        Epg epg = new Epg();
        for (EpgContentInfo epgContent : epgContents) {
            epg.getEpgContent().add(epgContent.createEpgContent());
        }
        platform.setEpg(epg);
        
        platform.setId(id);
        
        return platform;
    }
}

class ServiceProviderInfo {
    private List<RegionalisationInfo> regions = Lists.newArrayList();

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

class RegionalisationInfo {
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

class RegionInfo {
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

class EpgContentInfo {
    private String channelId;
    private String channelNumber;
    private List<RegionalisationInfo> regions = Lists.newArrayList();
    
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    
    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }
    
    public void setRegions(List<RegionalisationInfo> regions) {
        this.regions = regions;
    }
    
    public EpgContent createEpgContent() {
        EpgContent epgContent = new EpgContent();
        
        epgContent.setChannelId(channelId);
        epgContent.setChannelNumber(channelNumber);
        
        RegionalisationList regionalisationList = new RegionalisationList();
        for (RegionalisationInfo regionalisation : regions) {
            regionalisationList.getRegionalisation().add(regionalisation.createRegionalisation());                
        }
        epgContent.setRegionalisationList(regionalisationList);
        
        return epgContent;
    }
}