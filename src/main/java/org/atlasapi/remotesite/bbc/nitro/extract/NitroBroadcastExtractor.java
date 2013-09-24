package org.atlasapi.remotesite.bbc.nitro.extract;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.BbcIonServices;
import org.joda.time.DateTime;

import com.google.common.base.Optional;

/**
 * Extracts a {@link com.metabroadcast.atlas.glycerin.model.Broadcast Atlas
 * Broadcast} from a {@link Broadcast Nitro Broadcast}.
 */
public class NitroBroadcastExtractor
    implements ContentExtractor<com.metabroadcast.atlas.glycerin.model.Broadcast, Optional<Broadcast>> {

    @Override
    public Optional<Broadcast> extract(com.metabroadcast.atlas.glycerin.model.Broadcast source) {
        String channel = BbcIonServices.get(source.getService().getSid());
        if (channel == null) {
            return Optional.absent();
        }
        DateTime start = NitroUtil.toDateTime(source.getPublishedTime().getStart());
        DateTime end = NitroUtil.toDateTime(source.getPublishedTime().getEnd());
        Broadcast broadcast = new Broadcast(channel, start, end)
            .withId("bbc:"+source.getPid());
        broadcast.setRepeat(source.isIsRepeat());
        broadcast.setAudioDescribed(source.isIsAudioDescribed());
        return Optional.of(broadcast);
    }

}
