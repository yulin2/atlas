package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

import org.joda.time.DateTime;

public class IonOndemandChanges extends IonFeed {

    private DateTime nextFromDatetime;
    private List<IonOndemandChange> blocklist;

    public List<IonOndemandChange> getBlocklist() {
        return blocklist;
    }

    public DateTime getNextFromDatetime() {
        return nextFromDatetime;
    }
    
}
