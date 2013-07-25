package org.atlasapi.remotesite.itv.whatson;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.joda.time.DateTime;

import com.google.common.collect.FluentIterable;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;


public class ItvWhatsOnUpdater {
    private final String feedUrl;
    private final RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient;
    
    public ItvWhatsOnUpdater(String feedUrl, RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient) {
        this(new SystemClock(),feedUrl, itvWhatsOnClient);
    }
    
    public ItvWhatsOnUpdater(Clock clock, String feedUrl, RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient) {
        this.feedUrl = feedUrl;
        this.itvWhatsOnClient = itvWhatsOnClient;
    }

    public void run(DateTime date) {
        System.out.println("Run for " + date.toString() + " would go here");
    }

}
