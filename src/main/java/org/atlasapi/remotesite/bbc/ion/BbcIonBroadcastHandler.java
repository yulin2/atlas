package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.metabroadcast.common.base.Maybe;

public interface BbcIonBroadcastHandler {

    Maybe<ItemAndPossibleBroadcast> handle(IonBroadcast broadcast);
    
}
