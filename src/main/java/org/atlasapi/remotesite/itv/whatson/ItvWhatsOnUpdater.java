package org.atlasapi.remotesite.itv.whatson;

import java.util.Collection;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.metabroadcast.common.scheduling.ScheduledTask;


public class ItvWhatsOnUpdater extends ScheduledTask {
    private final String feedUrl;
    private final RemoteSiteClient<Collection<ItvWhatsOnEntryDuration>> itvWhatsOnClient;
    
    public ItvWhatsOnUpdater(String feedUrl, RemoteSiteClient<Collection<ItvWhatsOnEntryDuration>> itvWhatsOnClient) {
        this.feedUrl = feedUrl;
        this.itvWhatsOnClient = itvWhatsOnClient;
    }

    @Override
    protected void runTask() {
        // TODO work out feed url

    }

}
