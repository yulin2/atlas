package org.atlasapi.query.content.schedule;

import java.util.List;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.ScheduleEntry;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class ScheduleOverlapResolver implements ScheduleResolver {

    private final ScheduleResolver scheduleResovler;
    private final AdapterLog log;
    private final ScheduleOverlapListener overlapListener;

    public ScheduleOverlapResolver(ScheduleResolver scheduleResovler, ScheduleOverlapListener overlapListener, AdapterLog log) {
        this.scheduleResovler = scheduleResovler;
        this.overlapListener = overlapListener;
        this.log = log;
    }

    @Override
    public Schedule schedule(DateTime from, DateTime to, Iterable<Channel> channels, Iterable<Publisher> publishers) {
        Schedule schedule = scheduleResovler.schedule(from, to, channels, publishers);

        ImmutableList.Builder<ScheduleChannel> scheduleChannels = ImmutableList.builder();
        for (ScheduleChannel channel : schedule.scheduleChannels()) {
            scheduleChannels.add(new ScheduleChannel(channel.channel(), processOverlaps(channel.items())));
        }

        return new Schedule(scheduleChannels.build(), schedule.interval());
    }

    public List<Item> processOverlaps(List<Item> items) {
        if (items.size() < 2) {
            return items;
        }
        
        ImmutableList.Builder<Item> results = ImmutableList.builder();
        Item beforePrevious = null;
        Item previous = Iterables.getFirst(items, null);
        
        boolean includeLast = true;
        for (int i = 1; i < items.size(); i++) {
            Item item = items.get(i);
            includeLast = true;
        
            Maybe<Interval> previousInterval = ScheduleEntry.BROADCAST.apply(previous).transmissionInterval();
            Maybe<Interval> currentInterval = ScheduleEntry.BROADCAST.apply(item).transmissionInterval();

            if (previousInterval.hasValue() && currentInterval.hasValue() && previousInterval.requireValue().overlaps(currentInterval.requireValue())) {
                OverlapResolution overlapResolution = resolveOverlap(beforePrevious, previous, item, Iterables.get(items, i + 1, null));
                if (! overlapResolution.shouldRemovePrevious()) {
                    results.add(previous);
                }
                if (overlapResolution.shouldRemoveCurrent()) {
                    i++;
                    includeLast = false;
                }
            } else {
                results.add(previous);
            }

            beforePrevious = previous;
            previous = item;
        }
        
        if (includeLast) {
            results.add(Iterables.getLast(items));
        }

        return results.build();
    }

    private OverlapResolution resolveOverlap(Item beforePrevious, Item previous, Item item, Item next) {
        Broadcast previousBroadcast = ScheduleEntry.BROADCAST.apply(previous);
        Broadcast broadcast = ScheduleEntry.BROADCAST.apply(item);

        if (Objects.equal(previousBroadcast.getId(), broadcast.getId()) && previousBroadcast.getLastUpdated() != null && broadcast.getLastUpdated() != null) {
            if (previousBroadcast.getLastUpdated().isAfter(broadcast.getLastUpdated())) {
                overlapListener.itemRemovedFromSchedule(item, broadcast);
                return OverlapResolution.removeCurrent();
            } else {
                overlapListener.itemRemovedFromSchedule(previous, previousBroadcast);
                return OverlapResolution.removePrevious();
            }
        }

        if (beforePrevious == null || next == null) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription(
                    "Unable to remove either " + previous + " or " + item + " as we don't have items before or after"));
            return OverlapResolution.removeNeither();
        }

        Maybe<Interval> beforeInterval = ScheduleEntry.BROADCAST.apply(beforePrevious).transmissionInterval();
        Maybe<Interval> previousInterval = previousBroadcast.transmissionInterval();
        Maybe<Interval> currentInterval = broadcast.transmissionInterval();
        Maybe<Interval> nextInterval = ScheduleEntry.BROADCAST.apply(next).transmissionInterval();

        if (beforeInterval.isNothing() || nextInterval.isNothing() || previousInterval.isNothing() || currentInterval.isNothing()) {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription(
                    "Unable to remove either " + previous + " or " + item + " as we can't find enough intervals to process"));
            return OverlapResolution.removeNeither();
        }

        if (!beforeInterval.requireValue().overlaps(previousInterval.requireValue()) && !previousInterval.requireValue().overlaps(nextInterval.requireValue())) {
            overlapListener.itemRemovedFromSchedule(item, broadcast);
            return OverlapResolution.removeCurrent();
        } else if (!beforeInterval.requireValue().overlaps(currentInterval.requireValue()) && !currentInterval.requireValue().overlaps(nextInterval.requireValue())) {
            overlapListener.itemRemovedFromSchedule(previous, previousBroadcast);
            return OverlapResolution.removePrevious();
        } else {
            log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription(
                    "Unable to remove either " + previous + " or " + item + " to stop overlapping broadcasts"));
            return OverlapResolution.removeNeither();
        }
    }
    
    static class OverlapResolution {
        
        static OverlapResolution removePrevious() {
            return new OverlapResolution(true, false);
        }
        
        static OverlapResolution removeCurrent() {
            return new OverlapResolution(false, true);
        }
        
        static OverlapResolution removeNeither() {
            return new OverlapResolution(false, false);
        }
        
        private final boolean removePrevious;
        private final boolean removeCurrent;

        public OverlapResolution(boolean removePrevious, boolean removeCurrent) {
            this.removePrevious = removePrevious;
            this.removeCurrent = removeCurrent;
        }
        
        public boolean shouldRemovePrevious() {
            return removePrevious;
        }
        
        public boolean shouldRemoveCurrent() {
            return removeCurrent;
        }
    }
}
