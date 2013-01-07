package org.atlasapi.remotesite.pa.channels;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelGroupWriter;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.pa.PaChannelMap;
import org.atlasapi.remotesite.pa.channels.bindings.EpgContent;
import org.atlasapi.remotesite.pa.channels.bindings.Regionalisation;
import org.atlasapi.remotesite.pa.channels.bindings.RegionalisationList;
import org.atlasapi.remotesite.pa.channels.bindings.ServiceProvider;
import org.atlasapi.remotesite.pa.channels.bindings.Station;
import org.atlasapi.remotesite.pa.channels.bindings.TvChannelData;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;

public class PaChannelsProcessor {
    
    static final String IMAGE_PREFIX = "http://images.atlas.metabroadcast.com/pressassociation.com/channels/";
    private static final String PLATFORM_ALIAS_PREFIX = "http://pressassociation.com/platforms/";
    private static final String REGION_ALIAS_PREFIX = "http://pressassociation.com/regions/";
    private static final String PLATFORM_PREFIX = "http://ref.atlasapi.org/platforms/";
    private static final String REGION_PREFIX = "http://ref.atlasapi.org/regions/";
    private static final String CHANNEL_URI_PREFIX = "http://ref.atlasapi.org/channels/";
    
    private final ChannelResolver channelResolver;
    private final ChannelWriter channelWriter;
    private final ChannelGroupResolver channelGroupResolver;
    private final ChannelGroupWriter channelGroupWriter;

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
            processStation(station);
        }
    }
    
    private void processStation(Station station) {
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
            throw new RuntimeException("Station with id " + station.getId() + " has no channels");
        }
    }

    List<Channel> processChildChannels(List<org.atlasapi.remotesite.pa.channels.bindings.Channel> channels) {
        Builder<Channel> children = ImmutableList.<Channel>builder();
        for (org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel : channels) {
            String childName = Iterables.getOnlyElement(paChannel.getNames().getName()).getvalue();
            String childKey = StringEscapeUtils.unescapeHtml(childName);
            childKey= CharMatcher.WHITESPACE.removeFrom(childKey);
            String childImage = IMAGE_PREFIX + Iterables.getOnlyElement(paChannel.getLogos().getLogo()).getvalue();

            Channel child = Channel.builder()
                    .withUri(CHANNEL_URI_PREFIX + childKey.toLowerCase())
                    .withTitle(childName)
                    .withImage(childImage)
                    .withSource(Publisher.METABROADCAST)
                    .build();
            child.addAlias(PaChannelMap.createUriFromId(paChannel.getId()));
            
            children.add(child);
        }
        return children.build();
    }

    Channel processParentChannel(Station station) {
     // station has no image block, so can't obtain the image from the station
        String parentName = Iterables.getOnlyElement(station.getNames().getName()).getvalue();
        String parentKey = StringEscapeUtils.unescapeHtml(parentName);
        parentKey= CharMatcher.WHITESPACE.removeFrom(parentKey);
        String parentImage = IMAGE_PREFIX /*+ Iterables.getOnlyElement(station.getLogos().getLogo()).getvalue()*/;

        Channel parentChannel = Channel.builder()
                .withUri(CHANNEL_URI_PREFIX + parentKey.toLowerCase())
                .withTitle(parentName)
                .withImage(parentImage)
                .withSource(Publisher.METABROADCAST)
                .build();
        
        parentChannel.addAlias(PaChannelMap.createUriFromId(station.getId()));
        
        return parentChannel;
    }

    Channel processStandaloneChannel(org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel) {

        String channelName = Iterables.getOnlyElement(paChannel.getNames().getName()).getvalue();
        String channelKey = StringEscapeUtils.unescapeHtml(channelName);
        channelKey= CharMatcher.WHITESPACE.removeFrom(channelKey);
        String image = IMAGE_PREFIX + Iterables.getOnlyElement(paChannel.getLogos().getLogo()).getvalue();
        
        Channel channel = Channel.builder()
                .withUri(CHANNEL_URI_PREFIX + channelKey.toLowerCase())
                .withImage(image)
                .withTitle(channelName)
                .withSource(Publisher.METABROADCAST)
                .build();
        channel.addAlias(PaChannelMap.createUriFromId(paChannel.getId()));
        
        return channel;
    }

    private Channel createOrMergeChannel(Channel newChannel) {
        Maybe<Channel> existing = channelResolver.forAlias(Iterables.getOnlyElement(newChannel.getAliases()));
        if (existing.hasValue()) {
            Channel existingChannel = existing.requireValue();
            
            existingChannel.setTitle(newChannel.title());
            existingChannel.setImage(newChannel.image());
            existingChannel.addAliases(newChannel.getAliases());
            if (newChannel.mediaType() != null) {
                existingChannel.setMediaType(newChannel.mediaType());
            }
            
            channelWriter.write(existingChannel);
            return existingChannel;
        } else {
            channelWriter.write(newChannel);
            return newChannel;
        }
    }

    private void processPlatforms(List<org.atlasapi.remotesite.pa.channels.bindings.Platform> platforms, List<ServiceProvider> serviceProviders, List<org.atlasapi.remotesite.pa.channels.bindings.Region> regions) {   
        for (org.atlasapi.remotesite.pa.channels.bindings.Platform paPlatform : platforms) {
            ServiceProvider serviceProvider = getServiceProvider(paPlatform.getServiceProviderId(), serviceProviders);
            List<ChannelGroup> channelGroups = processPlatform(paPlatform, serviceProvider, regions);
            for (ChannelGroup channelGroup : channelGroups) {
                createOrMergeChannelGroup(channelGroup);
            }
        }
    }

    List<ChannelGroup> processPlatform(org.atlasapi.remotesite.pa.channels.bindings.Platform paPlatform, ServiceProvider serviceProvider, List<org.atlasapi.remotesite.pa.channels.bindings.Region> regions) {
        List<ChannelGroup> channelGroups = Lists.newArrayList();
        
        String platformName = Iterables.getOnlyElement(paPlatform.getNames().getName()).getvalue();
        String platformKey = StringEscapeUtils.unescapeHtml(platformName);
        platformKey = CharMatcher.WHITESPACE.removeFrom(platformKey);

        Platform platform = new Platform();
        platform.setCanonicalUri(PLATFORM_PREFIX + platformKey.toLowerCase()); 
        platform.addAlias(PLATFORM_ALIAS_PREFIX + paPlatform.getId());
        platform.setTitle(platformName);
        platform.setPublisher(Publisher.METABROADCAST);
        
        List<Regionalisation> regionalisations = serviceProvider.getRegionalisationList().getRegionalisation();
        if (regionalisations.isEmpty()) {
            // If there are no regions, add the channels for the platform onto the platform directly.
            for (EpgContent epgContent : paPlatform.getEpg().getEpgContent()) {
                Maybe<Channel> resolved = channelResolver.forAlias(PaChannelMap.createUriFromId(epgContent.getChannelId()));
                if (!resolved.hasValue()) {
                    throw new RuntimeException("PA Channel with id " + epgContent.getChannelId() + " not found");
                }
                Channel channel = resolved.requireValue();
                
                ChannelNumbering channelNumbering = ChannelNumbering.builder()
                        .withChannel(channel)
                        .withChannelGroup(platform)
                        .withChannelNumber(Integer.parseInt(epgContent.getChannelNumber()))
                        .build();
                
                // add channel numbering to channel and write it
                channel.addChannelNumber(channelNumbering);
                createOrMergeChannel(channel);
                
                platform.addChannelNumbering(channelNumbering);
            }
            
            channelGroups.add(platform);
        } else {
            // If there are regions, create/update the regions as appropriate and then add the regions to the platform.
            for (Regionalisation regionalisation : regionalisations) {
                String regionName = null;
                for (org.atlasapi.remotesite.pa.channels.bindings.Region paRegion : regions) {
                    if (paRegion.getId().equals(regionalisation.getRegionId())) {
                        regionName = Iterables.getOnlyElement(paRegion.getNames().getName()).getvalue();
                        break;
                    }
                }
                if (regionName == null) {
                    throw new RuntimeException("Region with id " + regionalisation.getRegionId() + " not found in channel data file");
                }
                String regionKey = StringEscapeUtils.unescapeHtml(regionName);
                regionKey = CharMatcher.WHITESPACE.removeFrom(regionKey);
                
                Region region = new Region();
                region.setCanonicalUri(REGION_PREFIX + regionKey.toLowerCase());
                region.addAlias(REGION_ALIAS_PREFIX + regionalisation.getRegionId());
                region.setTitle(regionName);
                region.setPublisher(Publisher.METABROADCAST);
                
                setChannelsForRegion(region, regionalisation.getRegionId(), paPlatform.getEpg().getEpgContent());
                
                region.setPlatform(platform);
                platform.addRegion(region);
                channelGroups.add(region);
            }
            channelGroups.add(platform);
        }
        return channelGroups;
    }

    private ServiceProvider getServiceProvider(String serviceProviderId, List<ServiceProvider> serviceProviders) {
        for (ServiceProvider provider : serviceProviders) {
            if (provider.getId().equals(serviceProviderId)) {
                return provider;
            }
        }
        throw new RuntimeException("ServiceProvider with id " + serviceProviderId + " not found in the channel data file");
    }

    private void setChannelsForRegion(Region region, String regionId, List<EpgContent> epgContentList) {
        for (EpgContent epgContent : epgContentList) {
            RegionalisationList regionalisationList = epgContent.getRegionalisationList();
            if (regionalisationList != null && !regionalisationList.getRegionalisation().isEmpty()) {
                for (Regionalisation epgRegion : regionalisationList.getRegionalisation()) {
                    if (epgRegion.getRegionId().equals(regionId)) {
                     // channel is regionalised, and is in correct region
                        Maybe<Channel> resolved = channelResolver.forAlias(PaChannelMap.createUriFromId(epgContent.getChannelId()));
                        if (!resolved.hasValue()) {
                            throw new RuntimeException("PA Channel with id " + epgContent.getChannelId() + " not found");
                        }
                        
                        region.addChannelNumbering(createChannelNumbering(resolved.requireValue(), region, Integer.parseInt(epgContent.getChannelNumber())));
                        break;
                    }
                }
            } else {
                // channel not regionalised, add it to region
                Maybe<Channel> resolved = channelResolver.forAlias(PaChannelMap.createUriFromId(epgContent.getChannelId()));
                if (!resolved.hasValue()) {
                    throw new RuntimeException("PA Channel with id " + epgContent.getChannelId() + " not found");
                }
                region.addChannelNumbering(createChannelNumbering(resolved.requireValue(), region, Integer.parseInt(epgContent.getChannelNumber())));
            }
        }
    }

    private ChannelNumbering createChannelNumbering(Channel channel, Region region, int channelNumber) {
        ChannelNumbering channelNumbering = ChannelNumbering.builder()
                .withChannel(channel)
                .withChannelGroup(region)
                .withChannelNumber(channelNumber)
                .build();
        
        // add channel numbering to channel and write it
        channel.addChannelNumber(channelNumbering);
        createOrMergeChannel(channel);
        
        return channelNumbering;
    }

    private void createOrMergeChannelGroup(ChannelGroup channelGroup) {
        
        String alias = Iterables.getOnlyElement(channelGroup.getAliases());
        Optional<ChannelGroup> resolved = channelGroupResolver.fromAlias(alias);
        
        if (resolved.isPresent()) {
            ChannelGroup existing = resolved.get();

            existing.addAliases(channelGroup.getAliases());
            existing.setTitle(channelGroup.getTitle());
            if (channelGroup instanceof Platform) {
                if (existing instanceof Platform) {
                    ((Platform)existing).setRegionIds(((Platform)channelGroup).getRegions());
                } else {
                    throw new RuntimeException("new channelGroup with alias " + alias + " and type Platform does not match existing channelGroup of type " + existing.getClass());
                }
            }
            if (channelGroup instanceof Region) {
                if (existing instanceof Region) {
                    ((Region)existing).setPlatform(((Region)channelGroup).getPlatform());
                } else {
                    throw new RuntimeException("new channelGroup with alias " + alias + " and type Region does not match existing channelGroup of type " + existing.getClass());
                }
            }
            
            channelGroupWriter.store(existing);
        } else {
            channelGroupWriter.store(channelGroup);
        }
    }
}
