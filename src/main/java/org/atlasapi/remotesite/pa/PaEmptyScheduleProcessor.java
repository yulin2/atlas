package org.atlasapi.remotesite.pa;

import java.util.List;

import org.atlasapi.application.OldApplicationConfiguration;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.content.schedule.ScheduleHierarchy;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.remotesite.pa.listings.bindings.ProgData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.time.Timestamp;

public class PaEmptyScheduleProcessor implements PaProgDataProcessor {
    
    private final PaProgDataProcessor delegate;
    private final ScheduleResolver scheduleResolver;

    public PaEmptyScheduleProcessor(PaProgDataProcessor delegate, ScheduleResolver scheduleResolver) {
        this.delegate = delegate;
        this.scheduleResolver = scheduleResolver;
    }

    @Override
    public Optional<ScheduleHierarchy> process(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt) {
        return emptySlot(progData, channel, zone) ? delegate.process(progData, channel, zone, updatedAt)
                                                  : Optional.<ScheduleHierarchy>absent();
    }
    
    private boolean emptySlot(ProgData progData, Channel channel, DateTimeZone zone) {
        Duration duration = Duration.standardMinutes(Long.valueOf(progData.getDuration()));

        DateTime transmissionTime = PaProgrammeProcessor.getTransmissionTime(progData.getDate(), progData.getTime(), zone);
        DateTime transmissionEndTime = transmissionTime.plus(duration);
        
        Schedule schedule = scheduleResolver.schedule(transmissionTime, transmissionEndTime, ImmutableSet.of(channel), ImmutableSet.of(Publisher.PA), Optional.<OldApplicationConfiguration>absent());
        if (schedule.channelSchedules().isEmpty()) {
            return true;
        }
        
        List<Item> items = Lists.transform(Iterables.getOnlyElement(schedule.channelSchedules()).getEntries(), ItemAndBroadcast.toItem());
        return items.isEmpty();
    }
}
