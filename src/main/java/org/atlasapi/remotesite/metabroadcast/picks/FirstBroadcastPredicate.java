package org.atlasapi.remotesite.metabroadcast.picks;

import org.atlasapi.media.util.ItemAndBroadcast;

import com.google.common.base.Predicate;


/**
 * This predicate will match broadcasts that are either lacking a repeat
 * flag, or the repeat flag is set to false
 * 
 * @author tom
 *
 */
public class FirstBroadcastPredicate implements Predicate<ItemAndBroadcast> {

    @Override
    public boolean apply(ItemAndBroadcast input) {
        return input.getBroadcast().hasValue() 
                && !Boolean.TRUE.equals(input.getBroadcast().requireValue().getRepeat());
    }

}
