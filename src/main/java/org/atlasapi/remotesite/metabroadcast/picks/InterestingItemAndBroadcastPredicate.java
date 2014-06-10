package org.atlasapi.remotesite.metabroadcast.picks;

import java.util.Map;
import java.util.Set;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.util.ItemAndBroadcast;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.time.LocalTimeRange;

/**
 * A predicate to decide whether a given {@link ItemAndBroadcast} is for an item on
 * in primetime
 * 
 * @author tom
 *
 */
public class InterestingItemAndBroadcastPredicate implements Predicate<ItemAndBroadcast> {
    
    private static final LocalTimeRange CHILDRENS_PRIMETIME = new LocalTimeRange(new LocalTime(15, 0, 0), new LocalTime(19, 15, 0));
    private static final Map<String, LocalTimeRange> CHANNEL_PRIMETIME_OVERRIDES = ImmutableMap.of();
    
    private static final Map<String, LocalTimeRange> GENRE_OVERRIDES = ImmutableMap.of(
            AtlasGenre.CHILDRENS.getUri(), CHILDRENS_PRIMETIME,
            AtlasGenre.FACTUAL.getUri(), new LocalTimeRange(new LocalTime(0, 0, 0), new LocalTime(23, 59, 59)));
    
    private static final DateTimeZone UK_TIMEZONE = DateTimeZone.forID("Europe/London");
    private static final LocalTimeRange PRIMETIME = new LocalTimeRange(
            new LocalTime(19, 0, 0), new LocalTime(23, 15, 0));

    
    private final Set<String> channelUris;
    
    public InterestingItemAndBroadcastPredicate(Iterable<Channel> channels) {
        this.channelUris = ImmutableSet.copyOf(Iterables.transform(channels, Channel.TO_URI));
    }
    
    @Override
    public boolean apply(ItemAndBroadcast itemAndBroadcast) {
        
       
        Broadcast broadcast = itemAndBroadcast.getBroadcast().requireValue();
        Broadcast firstBroadcast = firstBroadcast(itemAndBroadcast.getItem());
        
        return isInterestingBroadcast(itemAndBroadcast.getItem(), broadcast)
                //caters for repeats
                || isInterestingBroadcast(itemAndBroadcast.getItem(), firstBroadcast);
    }
    
    private Broadcast firstBroadcast(Item item) {
        return TRANSMISSION_TIME_ORDERING.min(Item.FLATTEN_BROADCASTS.apply(item));
    }
    
    private boolean isInterestingBroadcast(Item item, Broadcast broadcast) {
        return channelUris.contains(broadcast.getBroadcastOn()) 
                && isPrimetime(item, broadcast); 
    }

    /**
     * A naive initial implementation. Further improvements may include having different 
     * primetimes for channels, as well as not assuming everything is in the UK timezone.
     * 
     * @param transmissionTime
     * @return
     */
    private boolean isPrimetime(Item item, Broadcast broadcast) {
        LocalTimeRange primetime = primetimeForGenres(item.getGenres())
                                    .or(primetimeForChannel(broadcast.getBroadcastOn()))
                                    .or(PRIMETIME);
        
        return primetime.contains(broadcast.getTransmissionTime().withZone(UK_TIMEZONE));
    }
    
    private Optional<LocalTimeRange> primetimeForGenres(Set<String> genres) {
        for (String genre : genres) {
            LocalTimeRange genreOverride = GENRE_OVERRIDES.get(genre);
            if (genreOverride != null) {
                return Optional.of(genreOverride);
            }
        }
        return Optional.absent();
    }
    
    private Optional<LocalTimeRange> primetimeForChannel(String channelUri) {
        return Optional.fromNullable(CHANNEL_PRIMETIME_OVERRIDES.get(channelUri));
    }
    
    private static Ordering<Broadcast> TRANSMISSION_TIME_ORDERING = new Ordering<Broadcast>() {

        @Override
        public int compare(Broadcast b1, Broadcast b2) {            
            return ComparisonChain.start()
                                  .compare(b1.getTransmissionTime(), b2.getTransmissionEndTime(), Ordering.natural().nullsLast())
                                  .result();
                                  
        }
        
    };
}
