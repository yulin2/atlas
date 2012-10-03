package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

public class IonVersionListFeed extends IonFeed {

    private List<IonVersion> blocklist;

    public List<IonVersion> getBlocklist() {
        return blocklist;
    }
    
}
