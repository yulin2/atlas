package org.atlasapi.remotesite.metabroadcast.picks;

import org.atlasapi.media.util.ItemAndBroadcast;

import com.google.common.base.Predicate;


/**
 * Since {@link Broadcast#getRepeat()} is set sparsely, at least for PA,
 * use this predicate with caution. It will only match if a broadcast 
 * has the flag present and it is false.
 * 
 * @author tom
 *
 */
public class FirstBroadcastPredicate implements Predicate<ItemAndBroadcast> {

    @Override
    public boolean apply(ItemAndBroadcast input) {
        return input.getBroadcast().hasValue() 
                && Boolean.FALSE.equals(input.getBroadcast().requireValue().getRepeat());
    }

}
