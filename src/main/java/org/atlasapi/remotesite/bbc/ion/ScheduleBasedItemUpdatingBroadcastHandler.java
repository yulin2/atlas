package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import com.metabroadcast.common.time.DateTimeZones;

public class ScheduleBasedItemUpdatingBroadcastHandler extends DefaultBbcIonBroadcastHandler {

    public ScheduleBasedItemUpdatingBroadcastHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        super(resolver, writer, log);
    }
    
    @Override
    protected boolean fullFetchPermitted(IonBroadcast broadcast, String itemUri) {
        LocalTime today = new DateTime(DateTimeZones.UTC).toLocalDate();
        LocalTime broadcastDay = broadcast.getStart().toLocalDate();
        
        String mediaType = broadcast.getMediaType();
        
        // today -4 for radio, today ± 2 for tv
        if (mediaType.equals("audio")) {
            return today.plusDays(1).isAfter(broadcastDay) && today.minusDays(5).isBefore(broadcastDay);    
        } else {
            return today.plusDays(3).isAfter(broadcastDay) && today.minusDays(3).isBefore(broadcastDay);
        }
    }
    
}
