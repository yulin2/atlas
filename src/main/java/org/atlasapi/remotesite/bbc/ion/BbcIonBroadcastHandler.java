package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

public interface BbcIonBroadcastHandler {

    void handle(IonBroadcast broadcast);
    
}
