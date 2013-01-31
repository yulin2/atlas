package org.atlasapi.remotesite.pa.channels;

import static org.atlasapi.remotesite.pa.channels.PaChannelGroupsIngester.getServiceProvider;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelWriter;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.pa.PaChannelMap;
import org.atlasapi.remotesite.pa.channels.bindings.Logo;
import org.atlasapi.remotesite.pa.channels.bindings.Name;
import org.atlasapi.remotesite.pa.channels.bindings.ProviderChannelId;
import org.atlasapi.remotesite.pa.channels.bindings.ServiceProvider;
import org.atlasapi.remotesite.pa.channels.bindings.Station;
import org.atlasapi.remotesite.pa.channels.bindings.Variation;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList.Builder;
import com.metabroadcast.common.base.Maybe;

public class PaChannelsIngester {

    private static final String REGIONAL_VARIATION = "regional";
    static final String IMAGE_PREFIX = "http://images.atlas.metabroadcast.com/pressassociation.com/channels/";
    private static final String CHANNEL_URI_PREFIX = "http://ref.atlasapi.org/channels/pressassociation.com/";
    private static final String STATION_ALIAS_PREFIX = "http://pressassociation.com/stations/";
    private static final String STATION_URI_PREFIX = "http://ref.atlasapi.org/channels/pressassociation.com/stations/";
    private static final String FORMAT_HD = "HD";
    private static final String YOUVIEW_SERVICE_PROVIDER_NAME = "YouView";
    private static final String YOUVIEW_CHANNEL_ALIAS_PREFIX = "http://youview.com/service/";
    private static final Map<String, MediaType> MEDIA_TYPE_MAPPING = ImmutableMap.<String, MediaType>builder()
        .put("TV", MediaType.VIDEO)
        .put("Radio", MediaType.VIDEO)
        .build();
    
    private final ChannelResolver channelResolver;
    private final ChannelWriter channelWriter;
    private final DateTimeFormatter formatter = ISODateTimeFormat.date();
    private final Logger log = LoggerFactory.getLogger(PaChannelsIngester.class);

    public PaChannelsIngester(ChannelResolver channelResolver, ChannelWriter channelWriter) {
        this.channelResolver = channelResolver;
        this.channelWriter = channelWriter;
    }
    
    public void processStations(List<Station> stations, List<ServiceProvider> serviceProviders) {
        for (Station station : stations) {
            try {
            if (!station.getChannels().getChannel().isEmpty()) {
                if (station.getChannels().getChannel().size() == 1) {
                    Channel channel = processStandaloneChannel(station.getChannels().getChannel().get(0), serviceProviders);
                    createOrMergeChannel(channel);
                } else {
                    Channel parentChannel = createOrMergeChannel(processParentChannel(station, station.getChannels().getChannel().get(0)));

                    List<Channel> children = processChildChannels(station.getChannels().getChannel(), serviceProviders);

                    for (Channel child : children) {
                        child.setParent(parentChannel);
                        createOrMergeChannel(child);
                    }
                }
            } else {
                log.error("Station with id " + station.getId() + " has no channels");
            }
            } catch (Exception e) {
                log.error("Exception thrown while processing station with id " + station.getId(), e);
            }
        }
    }
    
    List<Channel> processChildChannels(List<org.atlasapi.remotesite.pa.channels.bindings.Channel> channels, List<ServiceProvider> serviceProviders) {
        Builder<Channel> children = ImmutableList.<Channel>builder();
        for (org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel : channels) {
            children.add(processStandaloneChannel(paChannel, serviceProviders)); 
        }
        return children.build();
    }

    private String generateChannelKey(String id) {
        return "pa-channel-" + id;
    }

    private String generateStationKey(String id) {
        return "pa-station-" + id;
    }

    Channel processParentChannel(Station station, org.atlasapi.remotesite.pa.channels.bindings.Channel firstChild) {
        
        Channel parentChannel = Channel.builder()
            .withUri(STATION_URI_PREFIX + station.getId())
            .withKey(generateStationKey(station.getId()))
            .withSource(Publisher.METABROADCAST)
            .build();
        
        // will soon have station images, but do not currently, hence is commented out here
//        if (station.getLogos() != null) {
//            setChannelTitleAndImage(parentChannel, station.getNames().getName(), station.getLogos().getLogo());
//        } else {
        setChannelTitleAndImage(parentChannel, station.getNames().getName(), ImmutableList.<Logo>of());
//        }

        // MediaType and HD flag can't be obtained from the PA Station, so are taken from the first child channel
        // Regional always set to false on channels created from stations
        // Timeshift left as null as this is the channel from which children are considered to be timeshifted
        parentChannel.setMediaType(MEDIA_TYPE_MAPPING.get(firstChild.getMediaType()));
        parentChannel.setHighDefinition(getHighDefinition(firstChild.getFormat()));
        parentChannel.setRegional(false);
            
        parentChannel.addAlias(createStationUriFromId(station.getId()));
        
        return parentChannel;
    }

    private String createStationUriFromId(String id) {
        return STATION_ALIAS_PREFIX + id;
    }

    Channel processStandaloneChannel(org.atlasapi.remotesite.pa.channels.bindings.Channel paChannel, List<ServiceProvider> serviceProviders) {
        LocalDate startDate = formatter.parseLocalDate(paChannel.getStartDate());
        
        Channel channel = Channel.builder()
                .withUri(CHANNEL_URI_PREFIX + paChannel.getId())
                .withKey(generateChannelKey(paChannel.getId()))
                .withSource(Publisher.METABROADCAST)
                .withStartDate(startDate)
                .withEndDate(null)
                .build();
        
        if (paChannel.getProviderChannelIds() != null) {
            for (ProviderChannelId providerChannelId : paChannel.getProviderChannelIds().getProviderChannelId()) {
                channel.addAlias(lookupAlias(providerChannelId, serviceProviders));                
            }
        }
        
        if (paChannel.getMediaType() != null) {
            channel.setMediaType(MEDIA_TYPE_MAPPING.get(paChannel.getMediaType()));
        }
        
        channel.setHighDefinition(getHighDefinition(paChannel.getFormat()));
        
        List<Variation> variations = paChannel.getVariation();
        channel.setRegional(getRegional(variations));
        channel.setTimeshift(getTimeshift(variations));
        
        List<Logo> logos;
        if (paChannel.getLogos() != null) {
            logos = paChannel.getLogos().getLogo();
        } else {
            logos = ImmutableList.<Logo>of();
        }
        setChannelTitleAndImage(channel, paChannel.getNames().getName(), logos);
        
        channel.addAlias(PaChannelMap.createUriFromId(paChannel.getId()));
        
        return channel;
    }
    
    private Duration getTimeshift(List<Variation> variations) {
        for (Variation variation : variations) {
            if (variation.getTimeshift() != null) {
                return Duration.standardMinutes(Long.parseLong(variation.getTimeshift()));
            }
        }
        return null;
    }

    private Boolean getRegional(List<Variation> variations) {
        for (Variation variation : variations) {
            if (variation.getType().equals(REGIONAL_VARIATION)) {
                return true;
            }
        }
        return false;
    }

    private Boolean getHighDefinition(String format) {
        if (format != null) {
            return format.equals(FORMAT_HD);
        }
        return false;
    }

    private String lookupAlias(ProviderChannelId providerChannelId, List<ServiceProvider> serviceProviders) {
        ServiceProvider serviceProvider = getServiceProvider(providerChannelId.getServiceProviderId(), serviceProviders);
        if (serviceProvider == null) {
            throw new RuntimeException("ServiceProvider with id " + providerChannelId.getServiceProviderId() + " not found in the channel data file");
        }
        if (serviceProvider.getNames().getName().isEmpty()) {
            throw new RuntimeException("Service Provider with id " + providerChannelId.getServiceProviderId() + " has no name");
        }
        String serviceProviderName = Iterables.getOnlyElement(serviceProvider.getNames().getName()).getvalue();
        
        if (serviceProviderName.equals(YOUVIEW_SERVICE_PROVIDER_NAME)) {
            return youViewAlias(providerChannelId.getvalue());
        }
        
        throw new RuntimeException("service provider name " + serviceProviderName + " not recognised. Unable to process providerChannelId " + providerChannelId);
    }

    private String youViewAlias(String youViewChannelId) {
        return YOUVIEW_CHANNEL_ALIAS_PREFIX + youViewChannelId;
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
        String alias = Iterables.getFirst(newChannel.getAliases(), null);
        if (alias == null) {
            throw new RuntimeException("channel with uri " + newChannel.getCanonicalUri() + " has no aliases");
        }
        Maybe<Channel> existing = channelResolver.forAlias(alias);
        if (existing.hasValue()) {
            Channel existingChannel = existing.requireValue();

            existingChannel.setTitles(newChannel.allTitles());
            existingChannel.setImages(newChannel.allImages());
            existingChannel.setStartDate(newChannel.startDate());
            existingChannel.setEndDate(newChannel.endDate());
            existingChannel.addAliases(newChannel.getAliases());
            existingChannel.setParent(newChannel.parent());
            existingChannel.setMediaType(newChannel.mediaType());
            existingChannel.setHighDefinition(newChannel.highDefinition());
            existingChannel.setRegional(newChannel.regional());
            existingChannel.setTimeshift(newChannel.timeshift());
            
            return channelWriter.write(existingChannel);
        } else {
            return channelWriter.write(newChannel);
        }
    }
}
