package org.atlasapi.remotesite.pa.channels;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.channel.TemporalString;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.pa.channels.bindings.Epg;
import org.atlasapi.remotesite.pa.channels.bindings.EpgContent;
import org.atlasapi.remotesite.pa.channels.bindings.Name;
import org.atlasapi.remotesite.pa.channels.bindings.Names;
import org.atlasapi.remotesite.pa.channels.bindings.Regionalisation;
import org.atlasapi.remotesite.pa.channels.bindings.RegionalisationList;
import org.atlasapi.remotesite.pa.channels.bindings.ServiceProvider;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BasicChannelGroupIngestTest {

    private final PaChannelsProcessor processor = new PaChannelsProcessor(mock(ChannelResolver.class), mock(ChannelWriter.class), mock(ChannelGroupResolver.class), mock(ChannelGroupWriter.class));
    
    @Test
    public void testBasicPlatformIngest() {
        PlatformInfo platformInfo = new PlatformInfo();
        platformInfo.setName("Freeview", "2011-09-28");
        platformInfo.setId("3");
        platformInfo.setServiceProviderId("2");
        // create serviceProvider with no regions
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setId("2");
        serviceProvider.setRegions(Lists.<RegionalisationInfo>newArrayList());

        Platform platform = processor.processBasicPlatform(platformInfo.createPlatform());
        
        // test that platform fields are picked up ok
        assertEquals("Freeview", platform.getTitle());
        assertEquals(new TemporalString("Freeview", new LocalDate(2011, 9, 28), null), Iterables.getOnlyElement(platform.getAllTitles()));
        assertEquals("http://ref.atlasapi.org/platforms/pressassociation.com/3", platform.getCanonicalUri());
        assertEquals("http://pressassociation.com/platforms/3", Iterables.getOnlyElement(platform.getAliases()));
        assertEquals(Publisher.METABROADCAST, platform.getPublisher());
    }
    
    @Test
    public void testBasicRegionIngest() {
        // create serviceProvider with a region
        RegionalisationInfo regionalisation = new RegionalisationInfo();
        regionalisation.setRegionId("61");
        
        ServiceProviderInfo serviceProvider = new ServiceProviderInfo();
        serviceProvider.setRegions(Lists.newArrayList(regionalisation));
        
        // create regions
        RegionInfo south = new RegionInfo();
        south.setId("61");
        south.setName("South", "2009-01-28");
        
        RegionInfo yorkshire = new RegionInfo();
        yorkshire.setId("67");
        yorkshire.setName("Yorkshire", "2007-06-10");
        
        
        
        Map<String, Region> regions = processor.createRegionsForPlatform(ImmutableList.of(regionalisation.createRegionalisation()), ImmutableList.of(south.createRegion(), yorkshire.createRegion()));
        
        Region region = Iterables.getOnlyElement(regions.values());
        
        assertEquals("South", region.getTitle());
        assertEquals(new TemporalString("South", new LocalDate(2009, 1, 28), null), Iterables.getOnlyElement(region.getAllTitles()));
        assertEquals("http://ref.atlasapi.org/regions/pressassociation.com/61", region.getCanonicalUri());
        assertEquals("http://pressassociation.com/regions/61", Iterables.getOnlyElement(region.getAliases()));
        assertEquals(Publisher.METABROADCAST, region.getPublisher());
    }
}

class PlatformInfo {
    private String name;
    private String nameStartDate;
    private String id;
    private List<EpgContentInfo> epgContents = Lists.newArrayList();
    private String serviceProviderId;
    
    public void setName(String name, String startDate) {
        this.name = name;
        this.nameStartDate = startDate;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setEpgContents(List<EpgContentInfo> epgContents) {
        this.epgContents = epgContents;
    }
    
    public void setServiceProviderId(String id) {
        this.serviceProviderId = id;
    }
    
    public org.atlasapi.remotesite.pa.channels.bindings.Platform createPlatform() {
        org.atlasapi.remotesite.pa.channels.bindings.Platform platform = new org.atlasapi.remotesite.pa.channels.bindings.Platform();
        
        Names paPlatformNames = new Names();
        Name paPlatformName = new Name();
        paPlatformName.setvalue(name);
        paPlatformName.setStartDate(nameStartDate);
        paPlatformNames.getName().add(paPlatformName);
        platform.setNames(paPlatformNames);
        
        Epg epg = new Epg();
        for (EpgContentInfo epgContent : epgContents) {
            epg.getEpgContent().add(epgContent.createEpgContent());
        }
        platform.setEpg(epg);
        
        platform.setId(id);
        platform.setServiceProviderId(serviceProviderId);
        
        return platform;
    }
}

class ServiceProviderInfo {
    private List<RegionalisationInfo> regions = Lists.newArrayList();
    private String id;

    public void setRegions(List<RegionalisationInfo> regions) {
        this.regions = regions;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public ServiceProvider createServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider();
        
        RegionalisationList regionalisationList = new RegionalisationList();
        for (RegionalisationInfo regionalisation : regions) {
            regionalisationList.getRegionalisation().add(regionalisation.createRegionalisation());                
        }
        serviceProvider.setRegionalisationList(regionalisationList);
        
        serviceProvider.setId(id);
        
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
    private String nameStartDate;
    private String id;
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setName(String name, String nameStartDate) {
        this.name = name;
        this.nameStartDate = nameStartDate;
    }
    
    public org.atlasapi.remotesite.pa.channels.bindings.Region createRegion() {
        org.atlasapi.remotesite.pa.channels.bindings.Region region = new org.atlasapi.remotesite.pa.channels.bindings.Region();
        
        Names paRegionNames = new Names();
        Name paRegionName = new Name();
        paRegionName.setvalue(name);
        paRegionName.setStartDate(nameStartDate);
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