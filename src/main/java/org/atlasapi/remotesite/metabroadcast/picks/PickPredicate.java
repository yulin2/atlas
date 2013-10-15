package org.atlasapi.remotesite.metabroadcast.picks;

import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.LocalTimeRange;


public class PickPredicate implements Predicate<Item>{

    private static final DateTimeZone UK_TIMEZONE = DateTimeZone.forID("Europe/London");
    private static final LocalTimeRange PRIMETIME = new LocalTimeRange(
            new LocalTime(19, 0, 0), new LocalTime(23, 15, 0));
    
    private final Set<String> channelUris;
    
    public PickPredicate(Iterable<Channel> channels) {
        this.channelUris = ImmutableSet.copyOf(Iterables.transform(channels, Channel.TO_URI));
    }
    
    @Override
    public boolean apply(Item item) {
        Broadcast broadcast = Iterables.getOnlyElement(item.flattenBroadcasts());
        return channelUris.contains(broadcast.getBroadcastOn()) 
                && isPrimetime(broadcast.getTransmissionTime())
                && ( Boolean.TRUE.equals(broadcast.getPremiere()) 
                        || Boolean.TRUE.equals(broadcast.getNewEpisode()) 
                        || Boolean.TRUE.equals(broadcast.getNewSeries()) 
                   ); 
    }

    /**
     * A naive initial implementation. Further improvements may include having different 
     * primetimes for channels, as well as not assuming everything is in the UK timezone.
     * 
     * @param transmissionTime
     * @return
     */
    private boolean isPrimetime(DateTime transmissionTime) {
        return PRIMETIME.contains(transmissionTime.withZone(UK_TIMEZONE));
    }
}
