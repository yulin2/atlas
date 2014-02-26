package org.atlasapi.remotesite.metabroadcast.picks;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.util.ItemAndBroadcast;
import org.joda.time.Duration;

import com.google.common.base.Predicate;


/**
 * A predicate to filter broadcasts shorter than the given {@link org.joda.time.Duration}
 * 
 * @author tom
 *
 */
public class ShortBroadcastPredicate implements Predicate<ItemAndBroadcast> {
    
    private final Duration shortBroadcastLength;
    
    public ShortBroadcastPredicate(Duration shortBroadcastLength) {
        this.shortBroadcastLength = checkNotNull(shortBroadcastLength);
    }
    
    @Override
    public boolean apply(ItemAndBroadcast itemAndBroadcast) {
        if (itemAndBroadcast.getBroadcast().isNothing()) {
            return false;
        }
        
        return itemAndBroadcast.getBroadcast().requireValue().getBroadcastDuration() 
                < shortBroadcastLength.getStandardSeconds();
    }

}
