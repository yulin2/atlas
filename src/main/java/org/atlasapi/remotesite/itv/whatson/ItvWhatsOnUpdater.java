package org.atlasapi.remotesite.itv.whatson;

import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.ERROR;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.joda.time.LocalDate;
import com.google.common.collect.FluentIterable;

public class ItvWhatsOnUpdater {
    private final String feedUrl;
    private final RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient;
    private final AdapterLog log;
    
    public ItvWhatsOnUpdater(String feedUrl, RemoteSiteClient<FluentIterable<ItvWhatsOnEntry>> itvWhatsOnClient, AdapterLog log) {
        this.feedUrl = feedUrl;
        this.itvWhatsOnClient = itvWhatsOnClient;
        this.log = log;
    }
    
    private String getFeedForDate(LocalDate date) {
        return feedUrl
                .replace(":yyyy", String.valueOf(date.getYear()))
                .replace(":mm", String.format("%02d", date.getMonthOfYear()))
                .replace(":dd", String.format("%02d", date.getDayOfMonth()));
    }
    
    private FluentIterable<ItvWhatsOnEntry> getFeed(String uri) {
        try {
            return itvWhatsOnClient.get(uri);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(ERROR).withCause(e).withSource(getClass()).withDescription("Exception fetching feed at " + uri));
            return null;
        }
    }

    public void run(LocalDate date) {
        FluentIterable<ItvWhatsOnEntry> entries = getFeed(getFeedForDate(date));
        for (ItvWhatsOnEntry entry : entries) {
            
        }
       
    }

}
