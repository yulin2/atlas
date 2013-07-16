package org.atlasapi.remotesite.channel4.epg;

import java.util.List;
import java.util.Map;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;

public class ScheduleResolverBroadcastTrimmer implements BroadcastTrimmer {

    private final Logger log = LoggerFactory.getLogger(ScheduleResolverBroadcastTrimmer.class);
    private final Publisher publisher;
    private final ContentWriter writer;
    private final ScheduleResolver scheduleResolver;
	private final ContentResolver resolver;

    public ScheduleResolverBroadcastTrimmer(Publisher publisher, ScheduleResolver scheduleResolver, ContentResolver resolver, ContentWriter writer) {
        this.scheduleResolver = scheduleResolver;
        this.publisher = publisher;
		this.resolver = resolver;
        this.writer = writer;
    }

    public void trimBroadcasts(Interval scheduleInterval, Channel channel, List<ItemRefAndBroadcast> acceptableBroadcasts) {
        try {
            Map<String, ItemRefAndBroadcast> acceptableItemBroadcasts = index(acceptableBroadcasts);
            
            //Get items currently broadcast in the interval
            Schedule schedule = scheduleResolver.schedule(scheduleInterval.getStart(), scheduleInterval.getEnd(), ImmutableSet.of(channel), ImmutableSet.of(publisher), Optional.<ApplicationConfiguration>absent());

            //For each item, check that it's broadcasts are in correct in the acceptable set, set actively published false if not.
            for (Item itemEmbeddedInSchedule : Iterables.getOnlyElement(schedule.scheduleChannels()).items()) {
            	// load the item from the main db to avoid reading stale data
            	String itemEmbeddedInScheduleUri = itemEmbeddedInSchedule.getCanonicalUri();
                Maybe<Identified> maybeItem = resolver.findByCanonicalUris(ImmutableList.of(itemEmbeddedInScheduleUri)).get(itemEmbeddedInScheduleUri);
                if(!maybeItem.hasValue()) {
                    continue;
                }
                Item item = (Item) maybeItem.requireValue();
                boolean changed = false;
                for (Version version : item.nativeVersions()) {
                    for (Broadcast broadcast : version.getBroadcasts()) {
                        // double-check the broadcast is in the valid interval/channel
                        if (!(contained(broadcast, scheduleInterval) && broadcast.getBroadcastOn().equals(channel.getUri()))) {
                            continue;
                        }
                        ItemRefAndBroadcast acceptable = acceptableItemBroadcasts.get(broadcast.getSourceId());
                        if (!matchesAcceptable(itemEmbeddedInScheduleUri, broadcast, acceptable)) {
                            if(!Boolean.FALSE.equals(broadcast.isActivelyPublished())) {
                                broadcast.setIsActivelyPublished(false);
                                changed = true;
                            }
                        }
                    }
                }
                if (changed) {
                    writer.createOrUpdate(item);
                }
            }
        } catch (Exception e) {
            log.error("Exception attempting to trim broadcasts for " + channel.getTitle() + " between " + scheduleInterval, e);
        }
    }

    private boolean matchesAcceptable(String itemEmbeddedInScheduleUri, Broadcast broadcast,
            ItemRefAndBroadcast acceptable) {
        if (broadcast.getSourceId() == null) {
            return true;
        }
        return acceptable != null
            && itemEmbeddedInScheduleUri.equals(acceptable.getItemUri())
            && acceptable.getBroadcast().getTransmissionTime().equals(broadcast.getTransmissionTime());
    }

    private Map<String, ItemRefAndBroadcast> index(List<ItemRefAndBroadcast> acceptableBroadcasts) {
        return Maps.uniqueIndex(acceptableBroadcasts, new Function<ItemRefAndBroadcast, String>() {
            @Override
            public String apply(ItemRefAndBroadcast input) {
                return input.getBroadcast().getSourceId();
            }
        });
    }

    private boolean contained(Broadcast broadcast, Interval interval) {
        return !broadcast.getTransmissionTime().isBefore(interval.getStart())
            && broadcast.getTransmissionTime().isBefore(interval.getEnd());
    }
}
