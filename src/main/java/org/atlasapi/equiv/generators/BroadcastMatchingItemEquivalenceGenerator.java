package org.atlasapi.equiv.generators;

import java.util.Set;

import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.time.DateTimeZones;

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
    public ScoredEquivalents<Item> generate(Item content) {
        ScoredEquivalentsBuilder<Item> scores = DefaultScoredEquivalents.fromSource("broadcast");

        int broadcasts = 0;
        for (Version version : content.getVersions()) {
            for (Broadcast broadcast : activelyPublished(channelFilter(version.getBroadcasts()))) {
                broadcasts++;
                Schedule schedule = scheduleAround(broadcast, Sets.difference(supportedPublishers, ImmutableSet.of(content.getPublisher())));
                for (ScheduleChannel channel : schedule.scheduleChannels()) {
                    for (Item scheduleItem : channel.items()) {
                        if(scheduleItem instanceof Item && hasQualifyingBroadcast(scheduleItem, broadcast)) {
                            scores.addEquivalent((Item) scheduleItem, Score.valueOf(1.0));
                        }
                    }
                }

            }
        }
        return scale(scores.build(), broadcasts);
    }
    
    private Iterable<Broadcast> activelyPublished(Iterable<Broadcast> channelFilter) {
        return Iterables.filter(channelFilter, new Predicate<Broadcast>() {
            @Override
            public boolean apply(Broadcast input) {
                return input.isActivelyPublished();
            }
        });
    }

    private Iterable<Broadcast> channelFilter(Set<Broadcast> broadcasts) {
        return Iterables.filter(broadcasts, new Predicate<Broadcast>() {
            @Override
            public boolean apply(Broadcast input) {
                return !ignoredChannels.contains(input.getBroadcastOn()) && input.getTransmissionTime().isBefore(new DateTime(DateTimeZones.UTC).plusWeeks(1));
            }
        });
    }
    
    private static final Set<String> ignoredChannels = ImmutableSet.<String>builder()
        .add(Channel.BBC_ONE_NORTHERN_IRELAND.uri())
        .add(Channel.BBC_ONE_CAMBRIDGE.uri())
        .add(Channel.BBC_ONE_CHANNEL_ISLANDS.uri())
        .add(Channel.BBC_ONE_EAST.uri())
        .add(Channel.BBC_ONE_EAST_MIDLANDS.uri())
        .add(Channel.BBC_ONE_HD.uri())
        .add(Channel.BBC_ONE_NORTH_EAST.uri())
        .add(Channel.BBC_ONE_NORTH_WEST.uri())
        .add(Channel.BBC_ONE_OXFORD.uri())
        .add(Channel.BBC_ONE_SCOTLAND.uri())
        .add(Channel.BBC_ONE_SOUTH.uri())
        .add(Channel.BBC_ONE_SOUTH_EAST.uri())
        .add(Channel.BBC_ONE_WALES.uri())
        .add(Channel.BBC_ONE_SOUTH_WEST.uri())
        .add(Channel.BBC_ONE_WEST.uri())
        .add(Channel.BBC_ONE_WEST_MIDLANDS.uri())
        .add(Channel.BBC_ONE_EAST_YORKSHIRE.uri())
        .add(Channel.BBC_ONE_YORKSHIRE.uri())
        .add(Channel.BBC_TWO_NORTHERN_IRELAND.uri())
        .add(Channel.BBC_TWO_NORTHERN_IRELAND_ALALOGUE.uri())
        .add(Channel.BBC_TWO_SCOTLAND.uri())
        .add(Channel.BBC_TWO_WALES.uri())
        .add(Channel.BBC_TWO_WALES_ANALOGUE.uri())
        .add(Channel.BBC_RADIO_RADIO4_LW.uri())
     .build();
    
    private ScoredEquivalents<Item> scale(ScoredEquivalents<Item> scores, final int broadcasts) {
        return DefaultScoredEquivalents.fromMappedEquivs(scores.source(), Maps.transformValues(scores.equivalents(), Score.transformerFrom(new Function<Double, Double>() {
            @Override
            public Double apply(Double input) {
                return input / broadcasts;
            }
        })));
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
