package org.atlasapi.remotesite.metabroadcast.picks;

import java.util.Set;
import java.util.concurrent.Callable;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.caching.BackgroundComputingValue;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;


public class PicksChannelsSupplier implements Supplier<Set<Channel>> {

    private static Logger log = LoggerFactory.getLogger(PicksChannelsSupplier.class);
    
    private final NumberToShortStringCodec codec = new SubstitutionTableNumberCodec();
    private final ChannelResolver channelResolver;
    private final Integer channelGroupId;

    private final BackgroundComputingValue<Set<Channel>> pickChannels;
    
    public PicksChannelsSupplier(final ChannelGroupResolver channelGroupResolver, ChannelResolver channelResolver, 
            String pickChannelGroup) {
        
        this.channelGroupId = Strings.isNullOrEmpty(pickChannelGroup) ? null : codec.decode(pickChannelGroup).intValue();
        this.channelResolver = channelResolver;
        this.pickChannels = new BackgroundComputingValue<Set<Channel>>(Duration.standardHours(1), new Callable<Set<Channel>>() {

            @Override
            public Set<Channel> call() throws Exception {
                if (channelGroupId == null) {
                    log.warn("No channel group found for picks ingest. Job will do nothing.");
                    return ImmutableSet.of();
                }
                Optional<ChannelGroup> channelGroup = channelGroupResolver.channelGroupFor(Long.valueOf(channelGroupId));
                return currentChannelsIn(channelGroup.get());
            }
            
        });
        
        pickChannels.start();
    }
    
    @Override
    public Set<Channel> get() {
        return pickChannels.get();
    }
    
    private Set<Channel> currentChannelsIn(ChannelGroup channelGroup) {
        LocalDate today = new LocalDate(DateTimeZone.UTC);
        Builder<Channel> channels = ImmutableSet.builder();
        for(ChannelNumbering numbering: channelGroup.getChannelNumberings()) {
            if( (numbering.getStartDate() == null || numbering.getStartDate().isBefore(today)) 
                    && (numbering.getEndDate() == null || numbering.getEndDate().isAfter(today))) {
                
                Maybe<Channel> maybeChannel = channelResolver.fromId(numbering.getChannel());
                if(maybeChannel.hasValue()) {
                    channels.add(maybeChannel.requireValue());
                } else {
                    log.error("Could not find channel ID " + numbering.getChannel());
                }
            }
        }
        return channels.build();
    }

}
