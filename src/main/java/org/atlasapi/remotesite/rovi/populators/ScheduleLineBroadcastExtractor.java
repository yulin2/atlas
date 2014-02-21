package org.atlasapi.remotesite.rovi.populators;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.rovi.model.ScheduleLine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.base.Maybe;


public class ScheduleLineBroadcastExtractor {

    private static final String EUROPE_LONDON_TIMEZONE = "Europe/London";
    private static final String BROADCAST_URI_PREFIX = "http://rovicorp.com/broadcasts/";
    private static final String CHANNEL_URI_PREFIX = "http://rovicorp.com/channels/";

    private static final Logger log = LoggerFactory.getLogger(ScheduleLineBroadcastExtractor.class);
    
    
    private final ChannelResolver channelResolver;
    
    public ScheduleLineBroadcastExtractor(ChannelResolver channelResolver) {
        this.channelResolver = checkNotNull(channelResolver);
    }
    
    public Maybe<Broadcast> extract(ScheduleLine scheduleLine) {
        Maybe<Channel> channel = getChannel(scheduleLine);
        
        if (!channel.hasValue()) {
            log.info("Ignoring broadcast on Rovi source [{}] on {} at {} as no Atlas channel found", 
                    new Object[] { scheduleLine.getSourceId(), scheduleLine.getStartDate(), scheduleLine.getStartTime() } );
            return Maybe.nothing();
        }
        
        DateTime startTime = scheduleLine.getStartDate()
                                         .toLocalDateTime(scheduleLine.getStartTime())
                                         .toDateTime(DateTimeZone.forID(EUROPE_LONDON_TIMEZONE));
        
        DateTime endTime = startTime.plusSeconds(scheduleLine.getDuration());
        
        Broadcast broadcast = new Broadcast(channel.requireValue().getCanonicalUri(), startTime, endTime);
        broadcast.setCanonicalUri(BROADCAST_URI_PREFIX + scheduleLine.getScheduleId());
        
        return Maybe.just(broadcast);
    }
    
    private Maybe<Channel> getChannel(ScheduleLine scheduleLine) {
        if (scheduleLine.getSourceId() != null) {
            return channelResolver.forAlias(CHANNEL_URI_PREFIX + scheduleLine.getSourceId());
        }
        
        return Maybe.nothing();
    }
}
