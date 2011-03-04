package org.atlasapi.remotesite.channel4.epg;

import java.util.Collection;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class BroadcastTrimmer {

    private final Publisher publisher;
    private final ContentWriter writer;
    private final AdapterLog log;
    private final ScheduleResolver scheduleResolver;

    public BroadcastTrimmer(Publisher publisher, ScheduleResolver scheduleResolver, ContentWriter writer, AdapterLog log) {
        this.scheduleResolver = scheduleResolver;
        this.publisher = publisher;
        this.writer = writer;
        this.log = log;
    }

    public void trimBroadcasts(Interval scheduleInterval, Channel channel, Collection<String> acceptableIds) {
        try {
            //Get items currently broadcast in the interval
            Schedule schedule = scheduleResolver.schedule(scheduleInterval.getStart(), scheduleInterval.getEnd(), ImmutableSet.of(channel), ImmutableSet.of(publisher));

            //For each item, check that it's broadcasts are in correct in the acceptable set, set actively published false if not.
            for (Item item : Iterables.getOnlyElement(schedule.scheduleChannels()).items()) {
                boolean changed = false;
                for (Version version : item.nativeVersions()) {
                    for (Broadcast broadcast : version.getBroadcasts()) {
                        //double-check the broadcast is in the valid interval/channel
                        if(contained(broadcast, scheduleInterval) && broadcast.getBroadcastOn().equals(channel.uri())) {
                            if (broadcast.getId() != null && !acceptableIds.contains(broadcast.getId())) {
                                broadcast.setIsActivelyPublished(false);
                                changed = true;
                            }
                        }
                    }
                }
                if(changed) {
                    writer.createOrUpdate(item);
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withDescription("Exception attempting to trim broadcasts for " + channel.title() + " between " + scheduleInterval).withCause(e));
        }
    }

    private boolean contained(Broadcast broadcast, Interval interval) {
        return broadcast.getTransmissionTime().isAfter(interval.getStart().minusMillis(1)) && broadcast.getTransmissionTime().isBefore(interval.getEnd());
    }
}
