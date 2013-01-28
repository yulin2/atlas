package org.atlasapi.remotesite.pa.channels;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.media.channel.ChannelGroupResolver;
import org.atlasapi.persistence.media.channel.ChannelGroupWriter;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.persistence.media.channel.ChannelWriter;
import org.atlasapi.remotesite.pa.PaChannelMap;
import org.atlasapi.remotesite.pa.channels.bindings.EpgContent;
import org.atlasapi.remotesite.pa.channels.bindings.Logo;
import org.atlasapi.remotesite.pa.channels.bindings.Name;
import org.atlasapi.remotesite.pa.channels.bindings.Regionalisation;
import org.atlasapi.remotesite.pa.channels.bindings.RegionalisationList;
import org.atlasapi.remotesite.pa.channels.bindings.ServiceProvider;
import org.atlasapi.remotesite.pa.channels.bindings.Station;
import org.atlasapi.remotesite.pa.channels.bindings.TvChannelData;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public class PaChannelsProcessor {
    
    static final String IMAGE_PREFIX = "http://images.atlas.metabroadcast.com/pressassociation.com/channels/";
    private static final String PLATFORM_ALIAS_PREFIX = "http://pressassociation.com/platforms/";
    private static final String REGION_ALIAS_PREFIX = "http://pressassociation.com/regions/";
    private static final String PLATFORM_PREFIX = "http://ref.atlasapi.org/platforms/pressassociation.com/";
    private static final String REGION_PREFIX = "http://ref.atlasapi.org/regions/pressassociation.com/";
    private static final String CHANNEL_URI_PREFIX = "http://ref.atlasapi.org/channels/pressassociation.com/";
    private static final String STATION_ALIAS_PREFIX = "http://pressassociation.com/stations/";
    private static final String STATION_URI_PREFIX = "http://ref.atlasapi.org/channels/pressassociation.com/stations/";
    
    private final ChannelResolver channelResolver;
    private final ChannelWriter channelWriter;
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelGroupWriter channelGroupWriter;
    private final DateTimeFormatter formatter = ISODateTimeFormat.date();
    private final Logger log = LoggerFactory.getLogger(PaChannelsProcessor.class);
    
    public PaChannelsProcessor(ChannelResolver channelResolver, ChannelWriter channelWriter, ChannelGroupResolver channelGroupResolver, ChannelGroupWriter channelGroupWriter) {
        this.channelResolver = channelResolver;
        this.channelWriter = channelWriter;
        this.channelGroupResolver = channelGroupResolver;
        this.channelGroupWriter = channelGroupWriter;
    }
    
    public void process(TvChannelData channelData) {
        processStations(channelData.getStations().getStation());
        processPlatforms(channelData.getPlatforms().getPlatform(), channelData.getServiceProviders().getServiceProvider(), channelData.getRegions().getRegion());
    }
    
    private void processStations(List<Station> stations) {
        for (Station station : stations) {
            if (!station.getChannels().getChannel().isEmpty()) {
                if (station.getChannels().getChannel().size() == 1) {
                    Channel channel = processStandaloneChannel(station.getChannels().getChannel().get(0));
                    createOrMergeChannel(channel);
                } else {
                    Channel parentChannel = createOrMergeChannel(processParentChannel(station));

                    List<Channel> children = processChildChannels(station.getChannels().getChannel());

                    for (Channel child : children) {
                        child.setParent(parentChannel);
                        createOrMergeChannel(child);
                    }
                }
            } else {
                log.error("Station with id " + station.getId() + " has no channels");
            }
        }
    }
    
    List<Channel> processChildChannels(List<org.atlasapi.remotesite.pa.channels.bindings.Channel> channels) {
        Builder<Channel> children = ImmutableList.<Channel>builder();
        for (org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel : channels) {
             
            LocalDate startDate = formatter.parseLocalDate(paChannel.getStartDate());

            Channel child = Channel.builder()
                    .withUri(CHANNEL_URI_PREFIX + paChannel.getId())
                    .withKey(generateChannelKey(paChannel.getId()))
                    .withSource(Publisher.METABROADCAST)
                    .withStartDate(startDate)
                    .withEndDate(null)
                    .build();
            
            if (paChannel.getLogos() != null) {
                setChannelTitleAndImage(child, paChannel.getNames().getName(), paChannel.getLogos().getLogo());
            } else {
                setChannelTitleAndImage(child, paChannel.getNames().getName(), ImmutableList.<Logo>of());
            }
            
            // TODO new aliases
            child.addAliasUrl(PaChannelMap.createUriFromId(paChannel.getId()));
            
            children.add(child);
        }
        return children.build();
    }

    private String generateChannelKey(String id) {
        return "pa-channel-" + id;
    }

    private String generateStationKey(String id) {
        return "pa-station-" + id;
    }

    Channel processParentChannel(Station station) {
        
        Channel parentChannel = Channel.builder()
                .withUri(STATION_URI_PREFIX + station.getId())
                .withKey(generateStationKey(station.getId()))
                .withSource(Publisher.METABROADCAST)
                .withStartDate(null)
                .withEndDate(null)
                .build();
        
//        if (station.getLogos() != null) {
//            setChannelTitleAndImage(parentChannel, station.getNames().getName(), station.getLogos().getLogo());
//        } else {
            setChannelTitleAndImage(parentChannel, station.getNames().getName(), ImmutableList.<Logo>of());
//        }
        
         // TODO new aliases
        parentChannel.addAliasUrl(createStationUriFromId(station.getId()));
        
        return parentChannel;
    }

    private String createStationUriFromId(String id) {
        return STATION_ALIAS_PREFIX + id;
    }

    Channel processStandaloneChannel(org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel) {
        LocalDate startDate = formatter.parseLocalDate(paChannel.getStartDate());
        
        Channel channel = Channel.builder()
                .withUri(CHANNEL_URI_PREFIX + paChannel.getId())
                .withKey(generateChannelKey(paChannel.getId()))
                .withSource(Publisher.METABROADCAST)
                .withStartDate(startDate)
                .withEndDate(null)
                .build();
        
        if (paChannel.getLogos() != null) {
            setChannelTitleAndImage(channel, paChannel.getNames().getName(), paChannel.getLogos().getLogo());
        } else {
            setChannelTitleAndImage(channel, paChannel.getNames().getName(), ImmutableList.<Logo>of());
        }
        
        // TODO new aliases
        channel.addAliasUrl(PaChannelMap.createUriFromId(paChannel.getId()));
        
        return channel;
    }
    
    private void setChannelTitleAndImage(Channel channel, List<Name> names, List<Logo> images) {
        for (Name name : names) {
            LocalDate titleStartDate = formatter.parseLocalDate(name.getStartDate());
            if (name.getEndDate() != null) {
                LocalDate titleEndDate = formatter.parseLocalDate(name.getEndDate());
                channel.addTitle(name.getvalue(), titleStartDate, titleEndDate.plusDays(1));
            } else {
                channel.addTitle(name.getvalue(), titleStartDate);
            }
        }

        for (Logo logo : images) {
            LocalDate imageStartDate = formatter.parseLocalDate(logo.getStartDate());
            if (logo.getEndDate() != null) {
                LocalDate imageEndDate = formatter.parseLocalDate(logo.getEndDate());
                channel.addImage(IMAGE_PREFIX + logo.getvalue(), imageStartDate, imageEndDate.plusDays(1));
            } else {
                channel.addImage(IMAGE_PREFIX + logo.getvalue(), imageStartDate);
            }
        }    
    }
    
    private Channel createOrMergeChannel(Channel newChannel) {
        // TODO new aliases
        Maybe<Channel> existing = channelResolver.forAlias(Iterables.getOnlyElement(newChannel.getAliasUrls()));
        if (existing.hasValue()) {
            Channel existingChannel = existing.requireValue();

            existingChannel.setTitles(newChannel.allTitles());
            existingChannel.setImages(newChannel.allImages());
            existingChannel.setStartDate(newChannel.startDate());
            existingChannel.setEndDate(newChannel.endDate());
            existingChannel.addAliases(newChannel.getAliases());
            
            if (newChannel.mediaType() != null) {
                existingChannel.setMediaType(newChannel.mediaType());
            }
            if (newChannel.highDefinition() != null) {
                existingChannel.setHighDefinition(newChannel.highDefinition());
            }
            if (newChannel.parent() != null) {
                existingChannel.setParent(newChannel.parent());
            }
            
            return channelWriter.write(existingChannel);
        } else {
            return channelWriter.write(newChannel);
        }
    }

    private void processPlatforms(List<org.atlasapi.remotesite.pa.channels.bindings.Platform> platforms, List<ServiceProvider> serviceProviders, List<org.atlasapi.remotesite.pa.channels.bindings.Region> paRegions) {
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
            Map<String, Region> regions = createRegionsForPlatform(serviceProvider.getRegionalisationList().getRegionalisation(), paRegions);

            Map<String, Region> writtenRegionMap = Maps.newHashMap();
            for (Entry<String, Region> entry : regions.entrySet()) {
                entry.getValue().setPlatform(platform);
                Region region = (Region)mergeAndWriteChannelGroup(entry.getValue());
                writtenRegionMap.put(entry.getKey(), region);
            }
//            for (Region region : regions.values()) {
//                region.setPlatform(platform);
//                region = (Region)mergeAndWriteChannelGroup(region);
//            }
            
            for (EpgContent epgContent : paPlatform.getEpg().getEpgContent()) {
                addChannelNumberings(epgContent, writtenRegionMap);
            }
        }
    }
    
    private void addChannelNumberings(EpgContent epgContent, Map<String, Region> regions) {
        // resolve the channel
        Maybe<Channel> resolved = channelResolver.forAlias(PaChannelMap.createUriFromId(epgContent.getChannelId()));
        if (!resolved.hasValue()) {
            throw new RuntimeException("PA Channel with id " + epgContent.getChannelId() + " not found");
        }
        
        Channel channel = resolved.requireValue();
        LocalDate startDate = formatter.parseLocalDate(epgContent.getStartDate());
        LocalDate endDate;
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

    Map<String, Region> createRegionsForPlatform(List<Regionalisation> regionalisations, List<org.atlasapi.remotesite.pa.channels.bindings.Region> paRegions) {
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
            // TODO new aliases
            region.addAliasUrl(REGION_ALIAS_PREFIX + regionalisation.getRegionId());
            region.setPublisher(Publisher.METABROADCAST);
            
            regions.put(regionalisation.getRegionId(), region);
        }
        return regions;
    }

    Platform processBasicPlatform(org.atlasapi.remotesite.pa.channels.bindings.Platform paPlatform) {

        Platform platform = new Platform();
        platform.setCanonicalUri(PLATFORM_PREFIX + paPlatform.getId());
        // TODO new aliases
        platform.addAliasUrl(PLATFORM_ALIAS_PREFIX + paPlatform.getId());
        platform.setPublisher(Publisher.METABROADCAST);
        
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

    private ServiceProvider getServiceProvider(String serviceProviderId, List<ServiceProvider> serviceProviders) {
        for (ServiceProvider provider : serviceProviders) {
            if (provider.getId().equals(serviceProviderId)) {
                return provider;
            }
        }
        return null;
    }

    private ChannelGroup mergeAndWriteChannelGroup(ChannelGroup channelGroup) {
        // TODO new aliases
        String alias = Iterables.getOnlyElement(channelGroup.getAliasUrls());
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
    
    class ChannelDetails {
        private Channel channel;
        private int channelNumber;
        private LocalDate startDate;
        private LocalDate endDate;
        
        ChannelDetails(Channel channel, int channelNumber, LocalDate startDate, LocalDate endDate) {
            this.channel = channel;
            this.channelNumber = channelNumber;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Channel getChannel() {
            return channel;
        }
        
        public int getChannelNumber() {
            return channelNumber;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
