package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.metabroadcast.common.base.Maybe;

public interface BbcIonBroadcastHandler {

    Maybe<ItemAndBroadcast> handle(IonBroadcast broadcast);
    
}
