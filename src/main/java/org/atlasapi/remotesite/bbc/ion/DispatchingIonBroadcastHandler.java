package org.atlasapi.remotesite.bbc.ion;

import java.util.List;

import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.google.common.collect.ImmutableList;

public class DispatchingIonBroadcastHandler implements BbcIonBroadcastHandler {

    private List<BbcIonBroadcastHandler> delegates;

    public DispatchingIonBroadcastHandler(BbcIonBroadcastHandler... handlers) {
        this.delegates = ImmutableList.copyOf(handlers);
    }
    
    @Override
    public void handle(IonBroadcast broadcast) {
        for (BbcIonBroadcastHandler delegate : delegates) {
            delegate.handle(broadcast);
        }
    }

}
