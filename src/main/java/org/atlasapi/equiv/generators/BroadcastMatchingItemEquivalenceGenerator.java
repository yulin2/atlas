package org.atlasapi.equiv.generators;

import java.util.Set;

import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.results.ScoredEquivalents.ScoredEquivalentsBuilder;
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

import com.google.common.collect.ImmutableSet;

public class BroadcastMatchingItemEquivalenceGenerator implements ContentEquivalenceGenerator<Item>{

    private final ScheduleResolver resolver;
    private final Set<Publisher> supportedPublishers;
    private final Duration flexibility;

    public BroadcastMatchingItemEquivalenceGenerator(ScheduleResolver resolver, Set<Publisher> supportedPublishers, Duration flexibility) {
        this.resolver = resolver;
        this.supportedPublishers = supportedPublishers;
        this.flexibility = flexibility;
    }
    
    @Override
    public ScoredEquivalents<Item> generateEquivalences(Item content) {
        
        ScoredEquivalentsBuilder<Item> scores = ScoredEquivalents.fromSource("broadcast");
        
        for (Version version : content.getVersions()) {
            for (Broadcast broadcast : version.getBroadcasts()) {
                
                Schedule schedule = scheduleAround(broadcast, supportedPublishers);
                for (ScheduleChannel channel : schedule.scheduleChannels()) {
                    for (Item scheduleItem : channel.items()) {
                        if(scheduleItem instanceof Item && hasQualifyingBroadcast(scheduleItem, broadcast)) {
                            scores.addEquivalent((Item) scheduleItem, 1.0);
                        }
                    }
                }
                
            }
        }
        
        return scores.build();
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
        return transmissionTime.isAfter(transmissionTime2.minus(flexibility)) && transmissionTime.isBefore(transmissionTime2.plus(flexibility));
    }

    private Schedule scheduleAround(Broadcast broadcast, Set<Publisher> publishers) {
        DateTime start = broadcast.getTransmissionTime().minus(flexibility);
        DateTime end = broadcast.getTransmissionEndTime().plus(flexibility);
        Channel channel = Channel.fromUri(broadcast.getBroadcastOn()).requireValue();
        
        return resolver.schedule(start, end, ImmutableSet.of(channel), publishers);
    }
}
