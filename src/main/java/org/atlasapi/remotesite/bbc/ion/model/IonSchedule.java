package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

public class IonSchedule extends IonFeed {

    private List<IonBroadcast> blocklist;

    public List<IonBroadcast> getBlocklist() {
        return blocklist;
    }

}
