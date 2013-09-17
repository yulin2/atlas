package org.atlasapi.remotesite.bbc.nitro.extract;

import javax.xml.datatype.XMLGregorianCalendar;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.ion.BbcIonServices;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import com.metabroadcast.common.time.DateTimeZones;

/**
 * Extracts a {@link com.metabroadcast.atlas.glycerin.model.Broadcast Atlas
 * Broadcast} from a {@link Broadcast Nitro Broadcast}.
 */
public class NitroBroadcastExtractor
    implements ContentExtractor<com.metabroadcast.atlas.glycerin.model.Broadcast, Broadcast> {

    @Override
    public Broadcast extract(com.metabroadcast.atlas.glycerin.model.Broadcast source) {
        String channel = BbcIonServices.get(source.getService().getSid());
        DateTime start = toDateTime(source.getPublishedTime().getStart());
        DateTime end = toDateTime(source.getPublishedTime().getEnd());
        Broadcast broadcast = new Broadcast(channel, start, end)
            .withId("bbc:"+source.getPid());
        broadcast.setRepeat(source.isIsRepeat());
        broadcast.setAudioDescribed(source.isIsAudioDescribed());
        return broadcast;
    }
    
    private DateTime toDateTime(XMLGregorianCalendar start) {
        return new DateTime(start.toGregorianCalendar(), ISOChronology.getInstance())
            .toDateTime(DateTimeZones.UTC);
    }

}
