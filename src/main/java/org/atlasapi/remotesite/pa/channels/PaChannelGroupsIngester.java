package org.atlasapi.remotesite.pa.channels;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.pa.PaChannelMap;
import org.atlasapi.remotesite.pa.channels.bindings.EpgContent;
import org.atlasapi.remotesite.pa.channels.bindings.Name;
import org.atlasapi.remotesite.pa.channels.bindings.Regionalisation;
import org.atlasapi.remotesite.pa.channels.bindings.RegionalisationList;
import org.atlasapi.remotesite.pa.channels.bindings.ServiceProvider;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.intl.Country;

public class PaChannelGroupsIngester {

    private static final String PLATFORM_ALIAS_PREFIX = "http://pressassociation.com/platforms/";
    private static final String REGION_ALIAS_PREFIX = "http://pressassociation.com/regions/";
    private static final String PLATFORM_PREFIX = "http://ref.atlasapi.org/platforms/pressassociation.com/";
    private static final String REGION_PREFIX = "http://ref.atlasapi.org/regions/pressassociation.com/";
    
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelGroupWriter channelGroupWriter;
    private final ChannelResolver channelResolver;
    private final ChannelWriter channelWriter;
    private final DateTimeFormatter formatter = ISODateTimeFormat.date();
    private final Logger log = LoggerFactory.getLogger(PaChannelGroupsIngester.class);
    
    public PaChannelGroupsIngester(ChannelGroupResolver channelGroupResolver, ChannelGroupWriter channelGroupWriter, ChannelResolver channelResolver, ChannelWriter channelWriter) {
        this.channelGroupResolver = channelGroupResolver;
        this.channelGroupWriter = channelGroupWriter;
        this.channelResolver = channelResolver;
        this.channelWriter = channelWriter;
    }
    
    public void processPlatforms(List<org.atlasapi.remotesite.pa.channels.bindings.Platform> platforms, List<ServiceProvider> serviceProviders, List<org.atlasapi.remotesite.pa.channels.bindings.Region> paRegions) {
        try {
            for (org.atlasapi.remotesite.pa.channels.bindings.Platform paPlatform : platforms) {
                processPlatform(paPlatform, serviceProviders, paRegions);
            }
        } catch (Exception e) {
            // prevent exceptions from killing file ingest
            log.error(e.getMessage(), e);
        }
    }
    
    private void processPlatform(org.atlasapi.remotesite.pa.channels.bindings.Platform paPlatform, List<ServiceProvider> serviceProviders, List<org.atlasapi.remotesite.pa.channels.bindings.Region> paRegions) {
        ServiceProvider serviceProvider = getServiceProvider(paPlatform.getServiceProviderId(), serviceProviders);
        if (serviceProvider == null) {
            log.error("ServiceProvider with id " + paPlatform.getServiceProviderId() + " not found in the channel data file");
            return;
        }
        
        // create and write basic platform
        Platform platform = processBasicPlatform(paPlatform);
        platform = (Platform)mergeAndWriteChannelGroup(platform);

        if (serviceProvider.getRegionalisationList() == null || serviceProvider.getRegionalisationList().getRegionalisation().isEmpty()) {
            addChannelsToPlatform(platform, paPlatform.getEpg().getEpgContent());
        } else {
            Map<String, Region> regions = createRegionsForPlatform(serviceProvider.getRegionalisationList().getRegionalisation(), paRegions, platform.getAvailableCountries());

            Map<String, Region> writtenRegionMap = Maps.newHashMap();
            for (Entry<String, Region> entry : regions.entrySet()) {
                entry.getValue().setPlatform(platform);
                Region region = (Region)mergeAndWriteChannelGroup(entry.getValue());
                writtenRegionMap.put(entry.getKey(), region);
            }
            
            for (EpgContent epgContent : paPlatform.getEpg().getEpgContent()) {
                addChannelNumberings(epgContent, writtenRegionMap);
            }
        }
    }
    
    private void addChannelNumberings(EpgContent epgContent, Map<String, Region> regions) {
        Maybe<Channel> resolved = channelResolver.forAlias(PaChannelMap.createUriFromId(epgContent.getChannelId()));
        if (!resolved.hasValue()) {
            throw new RuntimeException("PA Channel with id " + epgContent.getChannelId() + " not found");
        }
        
        Channel channel = resolved.requireValue();
        LocalDate startDate = formatter.parseLocalDate(epgContent.getStartDate());
        LocalDate endDate;
        // add a day, due to PA considering a date range to run from the start of startDate to the end of endDate,
        // whereas we consider a range to run from the start of startDate to the start of endDate
        if (epgContent.getEndDate() != null) {
            endDate = formatter.parseLocalDate(epgContent.getEndDate());
            endDate.plusDays(1);
        } else {
            endDate = null;
        }
        int channelNumber = Integer.parseInt(epgContent.getChannelNumber());
        
        RegionalisationList regionalisationList = epgContent.getRegionalisationList();
        if (regionalisationList == null) {
            // add to all regions
            for (Region region : regions.values()) {
                channel.addChannelNumber(region, channelNumber, startDate, endDate);
            }
        } else {
            // add to selected regions
            for (Regionalisation epgRegion : regionalisationList.getRegionalisation()) {
                channel.addChannelNumber(regions.get(epgRegion.getRegionId()), channelNumber, startDate, endDate);
            }
        }
        
        channelWriter.write(channel);
    }

    Map<String, Region> createRegionsForPlatform(List<Regionalisation> regionalisations, List<org.atlasapi.remotesite.pa.channels.bindings.Region> paRegions, Set<Country> countries) {
        Map<String, Region> regions = Maps.newHashMap();
        // If there are regions, create/update the regions as appropriate and then add the regions to the platform.
        for (Regionalisation regionalisation : regionalisations) {
            Region region = new Region();            
            for (org.atlasapi.remotesite.pa.channels.bindings.Region paRegion : paRegions) {
                if (paRegion.getId().equals(regionalisation.getRegionId())) {
                    for (Name name : paRegion.getNames().getName()) {
                        LocalDate titleStartDate = formatter.parseLocalDate(name.getStartDate());
                        if (name.getEndDate() != null) {
                            LocalDate titleEndDate = formatter.parseLocalDate(name.getEndDate());
                            region.addTitle(name.getvalue(), titleStartDate, titleEndDate.plusDays(1));
                        } else {
                            region.addTitle(name.getvalue(), titleStartDate);
                        }
                    }
                    break;
                }
            }
            
            region.setCanonicalUri(REGION_PREFIX + regionalisation.getRegionId());
            region.addAlias(REGION_ALIAS_PREFIX + regionalisation.getRegionId());
            region.setPublisher(Publisher.METABROADCAST);
            region.setAvailableCountries(countries);            
            regions.put(regionalisation.getRegionId(), region);
        }
        return regions;
    }

    Platform processBasicPlatform(org.atlasapi.remotesite.pa.channels.bindings.Platform paPlatform) {

        Platform platform = new Platform();
        platform.setCanonicalUri(PLATFORM_PREFIX + paPlatform.getId());
        platform.addAlias(PLATFORM_ALIAS_PREFIX + paPlatform.getId());
        platform.setPublisher(Publisher.METABROADCAST);
        
        if (paPlatform.getCountries() != null) {
            for (org.atlasapi.remotesite.pa.channels.bindings.Country country : paPlatform.getCountries().getCountry()) {
                platform.addAvailableCountry(Countries.fromCode(country.getCode()));
            }
        }
        
        for (Name name : paPlatform.getNames().getName()) {
            LocalDate titleStartDate = formatter.parseLocalDate(name.getStartDate());
            if (name.getEndDate() != null) {
                LocalDate titleEndDate = formatter.parseLocalDate(name.getEndDate());
                platform.addTitle(name.getvalue(), titleStartDate, titleEndDate.plusDays(1));
            } else {
                platform.addTitle(name.getvalue(), titleStartDate);
            }
        }
        
        return platform;
    }

    private void addChannelsToPlatform(Platform platform, List<EpgContent> epgContents) {
        for (EpgContent epgContent : epgContents) {
            Maybe<Channel> resolved = channelResolver.forAlias(PaChannelMap.createUriFromId(epgContent.getChannelId()));
            if (!resolved.hasValue()) {
                throw new RuntimeException("PA Channel with id " + epgContent.getChannelId() + " not found");
            }
            Channel channel = resolved.requireValue();
            LocalDate startDate = formatter.parseLocalDate(epgContent.getStartDate());
            LocalDate endDate;
            // add a day, due to PA considering a date range to run from the start of startDate to the end of endDate,
            // whereas we consider a range to run from the start of startDate to the start of endDate
            if (epgContent.getEndDate() != null) {
                endDate = formatter.parseLocalDate(epgContent.getEndDate());
                endDate.plusDays(1);
            } else {
                endDate = null;
            }
            
            channel.addChannelNumber(platform, Integer.parseInt(epgContent.getChannelNumber()), startDate, endDate);
            channelWriter.write(channel);
        }
    }

    public static ServiceProvider getServiceProvider(String serviceProviderId, List<ServiceProvider> serviceProviders) {
        for (ServiceProvider provider : serviceProviders) {
            if (provider.getId().equals(serviceProviderId)) {
                return provider;
            }
        }
        return null;
    }

    private ChannelGroup mergeAndWriteChannelGroup(ChannelGroup channelGroup) {
        String alias = Iterables.getOnlyElement(channelGroup.getAliases());
        Optional<ChannelGroup> resolved = channelGroupResolver.fromAlias(alias);
        
        if (resolved.isPresent()) {
            ChannelGroup existing = resolved.get();

            existing.addAliases(channelGroup.getAliases());
            existing.setTitles(channelGroup.getAllTitles());
            
            if (channelGroup instanceof Region) {
                if (existing instanceof Region) {
                    ((Region)existing).setPlatform(((Region)channelGroup).getPlatform());
                } else {
                    throw new RuntimeException("new channelGroup with alias " + alias + " and type Region does not match existing channelGroup of type " + existing.getClass());
                }
            }
            
            return channelGroupWriter.store(existing);
        } else {
            return channelGroupWriter.store(channelGroup);
        }
    }
}
