package org.atlasapi.remotesite.bbc.ion;

import java.util.List;

import org.atlasapi.media.util.ItemAndBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class DispatchingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private List<BbcIonBroadcastHandler> delegates;

    public DispatchingIonBroadcastHandler(BbcIonBroadcastHandler... handlers) {
        this.delegates = ImmutableList.copyOf(handlers);
    }
    
    @Override
    public Maybe<ItemAndBroadcast> handle(IonBroadcast broadcast) {
        for (BbcIonBroadcastHandler delegate : delegates) {
            delegate.handle(broadcast);
        }
    }

}
