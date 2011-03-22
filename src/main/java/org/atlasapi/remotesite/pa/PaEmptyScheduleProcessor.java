package org.atlasapi.remotesite.pa;

import java.util.List;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class PaEmptyScheduleProcessor implements PaProgDataProcessor {
    
    private final PaProgDataProcessor delegate;
    private final ScheduleResolver scheduleResolver;
    private final PaChannelMap channelMap = new PaChannelMap();

    public PaEmptyScheduleProcessor(PaProgDataProcessor delegate, ScheduleResolver scheduleResolver) {
        this.delegate = delegate;
        this.scheduleResolver = scheduleResolver;
    }

    @Override
    public void process(ProgData progData, ChannelData channelData, DateTimeZone zone) {
        Maybe<Channel> channel = channelMap.getChannel(Integer.valueOf(channelData.getChannelId()));
        if (channel.hasValue()) {
            if (emptySlot(progData, channel.requireValue(), zone)) {
                delegate.process(progData, channelData, zone);
            }
        }
    }
    
    private boolean emptySlot(ProgData progData, Channel channel, DateTimeZone zone) {
        Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));

        DateTime transmissionTime = PaProgrammeProcessor.getTransmissionTime(progData.getDate(), progData.getTime(), zone);
        DateTime transmissionEndTime = transmissionTime.plus(duration);
        
        Schedule schedule = scheduleResolver.schedule(transmissionTime, transmissionEndTime, ImmutableSet.of(channel), ImmutableSet.of(Publisher.PA));
        if (schedule.scheduleChannels().isEmpty()) {
            return true;
        }
        
        List<Item> items = Iterables.getOnlyElement(schedule.scheduleChannels()).items();
        return items.isEmpty();
    }
}
