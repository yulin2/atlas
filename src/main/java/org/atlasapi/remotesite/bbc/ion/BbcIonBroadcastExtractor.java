package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;

import com.metabroadcast.common.base.Maybe;

public class BbcIonBroadcastExtractor implements ContentExtractor<IonBroadcast, Maybe<Broadcast>> {

    private static final String BBC_CURIE_BASE = "bbc:";
    
    @Override
    public Maybe<Broadcast> extract(IonBroadcast source) {
        String serviceUri = BbcIonServices.get(source.getBroadcastService());
        if (serviceUri == null) {
            return Maybe.nothing();
        } else {
            Broadcast broadcast = new Broadcast(serviceUri, source.getStart(), source.getEnd());
            broadcast.withId(BBC_CURIE_BASE + source.getId()).setScheduleDate(source.getDate().toLocalDate());
            broadcast.setLastUpdated(source.getUpdated());
            broadcast.setRepeat(source.getIsRepeat());
            broadcast.setAudioDescribed(source.getIsAudiodescribed());
            broadcast.setSigned(source.getIsSigned());
            return Maybe.just(broadcast);
        }
    }
    
}
