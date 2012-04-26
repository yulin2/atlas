package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.DateTimeZones;

public class ScheduleBasedItemUpdatingBroadcastHandler extends DefaultBbcIonBroadcastHandler {

    public ScheduleBasedItemUpdatingBroadcastHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        super(resolver, writer, log);
    }
    
    @Override
    protected boolean fullFetchPermitted(IonBroadcast broadcast, String itemUri) {
        DateTime now = new DateTime(DateTimeZones.UTC);
        return now.plusDays(2).isAfter(broadcast.getStart()) 
            && now.minusDays(2).isBefore(broadcast.getStart());
    }
    
}
