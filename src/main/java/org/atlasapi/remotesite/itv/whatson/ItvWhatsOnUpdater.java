package org.atlasapi.remotesite.itv.whatson;

import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.ERROR;
import java.util.List;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.joda.time.LocalDate;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DayRange;
import com.metabroadcast.common.time.DayRangeGenerator;
import com.metabroadcast.common.time.SystemClock;

public class ItvWhatsOnUpdater extends ScheduledTask {
    private final String feedUrl;
    private final RemoteSiteClient<List<ItvWhatsOnEntry>> itvWhatsOnClient;
    private final ItvWhatsOnEntryProcessor processor;
    private final int lookAhead;
    private final int lookBack;
    private final AdapterLog log;
    private Clock clock;
    
    private ItvWhatsOnUpdater(String feedUrl, 
            RemoteSiteClient<List<ItvWhatsOnEntry>> itvWhatsOnClient, 
            ItvWhatsOnEntryProcessor processor, 
            int lookAhead,
            int lookBack, 
            AdapterLog log,
            Clock clock) {
        this.feedUrl = feedUrl;
        this.itvWhatsOnClient = itvWhatsOnClient;
        this.processor = processor;
        this.lookBack = lookBack;
        this.lookAhead = lookAhead;
        this.log = log;
        this.clock = clock;
    }
    
    private String getFeedForDate(LocalDate date) {
        return feedUrl + date.toString("YYYY/MM/dd");
    }
    
    private List<ItvWhatsOnEntry> getFeed(String uri) {
        try {
            return itvWhatsOnClient.get(uri);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(ERROR).withCause(e).withSource(getClass()).withDescription("Exception fetching feed at " + uri));
            return null;
        }
    }

    public void ingestFeedFor(LocalDate date) {
        List<ItvWhatsOnEntry> entries = getFeed(getFeedForDate(date));
        for (ItvWhatsOnEntry entry : entries) {
            processor.createOrUpdateAtlasEntityFrom(entry);
        }
    }

    @Override
    protected void runTask() {
        DayRangeGenerator dateRangeGenerator = new DayRangeGenerator()
        .withLookBack(lookBack)
        .withLookAhead(lookAhead);
        DayRange dayRange = dateRangeGenerator.generate(clock.now().toLocalDate());
        for (LocalDate date : dayRange) {
            ingestFeedFor(date);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String feedUrl;
        private RemoteSiteClient<List<ItvWhatsOnEntry>> itvWhatsOnClient;
        private ItvWhatsOnEntryProcessor processor;
        private int lookAhead;
        private int lookBack;
        private AdapterLog log;
        private Clock clock = new SystemClock();
        
        public Builder withFeedUrl(String feedUrl) {
            this.feedUrl = feedUrl;
            return this;
        }
        
        public Builder withWhatsOnClient(RemoteSiteClient<List<ItvWhatsOnEntry>> itvWhatsOnClient) {
            this.itvWhatsOnClient = itvWhatsOnClient;
            return this;
        }
        
        public Builder withProcessor(ItvWhatsOnEntryProcessor processor) {
            this.processor = processor;
            return this;
        }
        
        public Builder withLookAhead(int lookAhead) {
            this.lookAhead = lookAhead;
            return this;
        }
        
        public Builder withLookBack(int lookBack) {
            this.lookBack = lookBack;
            return this;
        }
        
        public Builder withLog(AdapterLog log) {
            this.log = log;
            return this;
        }
        
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }
        
        public ItvWhatsOnUpdater build() {
            return new ItvWhatsOnUpdater(feedUrl, 
                    itvWhatsOnClient, 
                    processor, 
                    lookAhead,
                    lookBack, 
                    log,
                    clock);
        }
    }
}
