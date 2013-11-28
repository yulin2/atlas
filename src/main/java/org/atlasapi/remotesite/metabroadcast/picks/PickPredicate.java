package org.atlasapi.remotesite.metabroadcast.picks;

import java.util.Map;
import java.util.Set;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.LocalTimeRange;


public class PickPredicate implements Predicate<Item>{

    private static final LocalTimeRange CHILDRENS_PRIMETIME = new LocalTimeRange(new LocalTime(15, 0, 0), new LocalTime(19, 15, 0));
    private static final Map<String, LocalTimeRange> CHANNEL_PRIMTIME_OVERRIDES = ImmutableMap.of(
            
    );
    
    private static final Map<String, LocalTimeRange> GENRE_OVERRIDES = ImmutableMap.of(
            AtlasGenre.CHILDRENS.getUri(), CHILDRENS_PRIMETIME,
            AtlasGenre.FACTUAL.getUri(), new LocalTimeRange(new LocalTime(0, 0, 0), new LocalTime(23, 59, 59)));
    
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
                && isPrimetime(item, broadcast)
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
        return Optional.fromNullable(CHANNEL_PRIMTIME_OVERRIDES.get(channelUri));
    }
}
