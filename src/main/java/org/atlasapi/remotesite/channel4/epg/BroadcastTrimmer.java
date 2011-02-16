package org.atlasapi.remotesite.channel4.epg;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Schedule.ScheduleEntry;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BroadcastTrimmer {

    private final KnownTypeQueryExecutor queryExecutor;
    private final Publisher publisher;
    private final ContentWriter writer;
    private final AdapterLog log;

    public BroadcastTrimmer(Publisher publisher, KnownTypeQueryExecutor queryExecutor, ContentWriter writer, AdapterLog log) {
        this.queryExecutor = queryExecutor;
        this.publisher = publisher;
        this.writer = writer;
        this.log = log;
    }

    public void trimBroadcasts(Interval scheduleInterval, Channel channel, Collection<String> acceptableIds) {
        try {

            Schedule schedule = queryExecutor.schedule(queryFor(scheduleInterval, channel));
            Set<Item> items = itemsFrom(schedule.getEntriesForOnlyChannel());

            for (Item item : items) {
                boolean changed = false;
                for (Version version : item.nativeVersions()) {
                    List<Broadcast> broadcasts = Lists.newLinkedList(version.getBroadcasts());
                    for (Broadcast broadcast : broadcasts) {
                        if (contained(broadcast, scheduleInterval) && broadcast.getBroadcastOn().equals(channel.uri())) { // double-check
                            if (broadcast.getId() != null && !acceptableIds.contains(broadcast.getId())) {
                                broadcasts.remove(broadcast);
                                changed = true;
                            }
                        }
                    }
                    version.setBroadcasts(Sets.newHashSet(broadcasts));
                }
                if(changed) {
                    writer.createOrUpdate(item);
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.WARN).withDescription("Exception attempting to trim broadcasts for " + channel.title() + " between " + scheduleInterval).withCause(e));
        }
    }

    private Set<Item> itemsFrom(List<ScheduleEntry> entries) {
        return ImmutableSet.copyOf(Iterables.transform(entries, Schedule.ScheduleEntry.TO_ITEM));
    }

    private boolean contained(Broadcast broadcast, Interval interval) {
        return broadcast.getTransmissionTime().isAfter(interval.getStart().minusMillis(1)) && broadcast.getTransmissionTime().isBefore(interval.getEnd());
    }

    private ContentQuery queryFor(Interval interval, Channel channel) {
        ImmutableSet<Publisher> publishers = ImmutableSet.of(publisher);
        Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery) Attributes.DESCRIPTION_PUBLISHER.createQuery(Operators.EQUALS, publishers),
                Attributes.BROADCAST_ON.createQuery(Operators.EQUALS, ImmutableList.of(channel.uri())),
                Attributes.BROADCAST_TRANSMISSION_TIME.createQuery(Operators.AFTER, ImmutableList.of(interval.getStart().minusMillis(1))),
                Attributes.BROADCAST_TRANSMISSION_TIME.createQuery(Operators.BEFORE, ImmutableList.of(interval.getEnd())));
        return new ContentQuery(queryAtoms).copyWithApplicationConfiguration(ApplicationConfiguration.DEFAULT_CONFIGURATION.copyWithIncludedPublishers(publishers));
    }

}
