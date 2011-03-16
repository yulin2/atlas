package org.atlasapi.equiv.tasks;

import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

public class BroadcastBasedItemEquivUpdater {

    private static final Set<Publisher> TARGET_PUBLISHERS = ImmutableSet.of(Publisher.BBC, Publisher.ITV, Publisher.C4, Publisher.FIVE);
    private static final Duration BROADCAST_FLEXIBILITY = Duration.standardMinutes(1);

    private final ScheduleResolver resolver;
    private double certainty = 0.9;
    private SymmetricItemVersionMerger itemVersionMerger;

    public BroadcastBasedItemEquivUpdater(ScheduleResolver resolver) {
        this.resolver = resolver;
        itemVersionMerger = new SymmetricItemVersionMerger();
    }

    public EquivResult<Item> updateEquivalence(Item item) {
        
        Multimap<Publisher, Item> binnedSuggestedItems = suggestEquivalentItems(item);
        
        SuggestedEquivalents<Item> equivSuggestions = SuggestedEquivalents.from(binnedSuggestedItems);
        
        EquivResult<Item> result = EquivResult.of(item, equivSuggestions, certainty);
        
        itemVersionMerger.mergeVersions(item, result.strongSuggestions().values());
        
        return result;
        
    }
    
    private Multimap<Publisher, Item> suggestEquivalentItems(Item item) {
        Multimap<Publisher, Item> binnedSuggestedItems = ArrayListMultimap.create();
        
        for (Version version : item.getVersions()) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                
                Schedule schedule = scheduleAround(broadcast);
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
        for (Version version : item.getVersions()) {
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

    private Schedule scheduleAround(Broadcast broadcast) {
        DateTime start = broadcast.getTransmissionTime().minus(BROADCAST_FLEXIBILITY);
        DateTime end = broadcast.getTransmissionEndTime().plus(BROADCAST_FLEXIBILITY);
        Channel channel = Channel.fromUri(broadcast.getBroadcastOn()).requireValue();
        
        return resolver.schedule(start, end, ImmutableSet.of(channel), TARGET_PUBLISHERS);
    }

    public BroadcastBasedItemEquivUpdater withCertaintyThreshold(double threshold) {
        this.certainty = threshold;
        return this;
    }
}
