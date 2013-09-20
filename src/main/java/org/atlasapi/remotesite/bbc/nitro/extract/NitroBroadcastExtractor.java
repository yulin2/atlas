package org.atlasapi.remotesite.bbc.nitro.extract;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.BbcIonServices;
import org.joda.time.DateTime;

/**
 * Extracts a {@link com.metabroadcast.atlas.glycerin.model.Broadcast Atlas
 * Broadcast} from a {@link Broadcast Nitro Broadcast}.
 */
public class NitroBroadcastExtractor
    implements ContentExtractor<com.metabroadcast.atlas.glycerin.model.Broadcast, Broadcast> {

    @Override
    public Broadcast extract(com.metabroadcast.atlas.glycerin.model.Broadcast source) {
        String channel = BbcIonServices.get(source.getService().getSid());
        DateTime start = NitroUtil.toDateTime(source.getPublishedTime().getStart());
        DateTime end = NitroUtil.toDateTime(source.getPublishedTime().getEnd());
        Broadcast broadcast = new Broadcast(channel, start, end)
            .withId("bbc:"+source.getPid());
        broadcast.setRepeat(source.isIsRepeat());
        broadcast.setAudioDescribed(source.isIsAudioDescribed());
        return broadcast;
    }

}
