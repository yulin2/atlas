package org.atlasapi.remotesite.itv.whatson;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.collect.FluentIterable;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class ItvWhatsOnUpdater extends ScheduledTask {
    private final String feedUrl;
    private final RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient;
    
    public ItvWhatsOnUpdater(String feedUrl, RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient) {
        this.feedUrl = feedUrl;
        this.itvWhatsOnClient = itvWhatsOnClient;
    }

    @Override
    protected void runTask() {
        // TODO work out feed url

    }

}
