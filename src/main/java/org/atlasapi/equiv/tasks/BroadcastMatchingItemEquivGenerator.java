package org.atlasapi.equiv.tasks;

import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class BroadcastMatchingItemEquivGenerator {

    private static final Duration BROADCAST_FLEXIBILITY = Duration.standardMinutes(1);
    
    private final ScheduleResolver resolver;

    public BroadcastMatchingItemEquivGenerator(ScheduleResolver resolver) {
        this.resolver = resolver;
    }
    
    public SuggestedEquivalents<Item> equivalentsFor(Item item, Iterable<Publisher> targetPublishers) {
        return SuggestedEquivalents.from(suggestEquivalentItems(item, ImmutableSet.copyOf(targetPublishers)));
    }
    
    private Multimap<Publisher, Item> suggestEquivalentItems(Item item, Set<Publisher> publishers) {
        Multimap<Publisher, Item> binnedSuggestedItems = ArrayListMultimap.create();
        
        for (Version version : item.getVersions()) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                
                Schedule schedule = scheduleAround(broadcast, publishers);
                for (ScheduleChannel channel : schedule.scheduleChannels()) {
                    for (Item scheduleItem : channel.items()) {
                        if(scheduleItem instanceof Item && hasQualifyingBroadcast(scheduleItem, broadcast)) {
                            binnedSuggestedItems.put(scheduleItem.getPublisher(), (Item) scheduleItem);
                        }
                    }
                }
                
            }
        }
        return binnedSuggestedItems;
    }
    
    private boolean hasQualifyingBroadcast(Item item, Broadcast referenceBroadcast) {
        for (Version version : item.nativeVersions()) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                if(around(broadcast, referenceBroadcast) && broadcast.getBroadcastOn() != null &&  broadcast.getBroadcastOn().equals(referenceBroadcast.getBroadcastOn())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean around(Broadcast broadcast, Broadcast referenceBroadcast) {
        return around(broadcast.getTransmissionTime(), referenceBroadcast.getTransmissionTime())
            && around(broadcast.getTransmissionEndTime(), referenceBroadcast.getTransmissionEndTime());
    }

    private boolean around(DateTime transmissionTime, DateTime transmissionTime2) {
        return transmissionTime.isAfter(transmissionTime2.minus(BROADCAST_FLEXIBILITY)) && transmissionTime.isBefore(transmissionTime2.plus(BROADCAST_FLEXIBILITY));
    }

    private Schedule scheduleAround(Broadcast broadcast, Set<Publisher> publishers) {
        DateTime start = broadcast.getTransmissionTime().minus(BROADCAST_FLEXIBILITY);
        DateTime end = broadcast.getTransmissionEndTime().plus(BROADCAST_FLEXIBILITY);
        Channel channel = Channel.fromUri(broadcast.getBroadcastOn()).requireValue();
        
        return resolver.schedule(start, end, ImmutableSet.of(channel), publishers);
    }
}
