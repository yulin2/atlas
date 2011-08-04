package org.atlasapi.remotesite.itv.interlinking;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;

public class ItvInterlinkingUpdater extends ScheduledTask {
    
    private final ItvInterlinkingSingleFileUpdater fileUpdater;
    private final AdapterLog log;
    private final int numberOfDays;

    public ItvInterlinkingUpdater(ItvInterlinkingSingleFileUpdater fileUpdater, AdapterLog log, int numberOfDays) {
        this.fileUpdater = fileUpdater;
        this.log = log;
        this.numberOfDays = numberOfDays;
    }

    @Override
    protected void runTask() {
        DateTime now = new SystemClock().now();
        
        int i = 0;
        
        while(shouldContinue() && i < numberOfDays) {
            String urlForDate = fileUpdater.getUrlForDate(now.minus(Duration.standardDays(numberOfDays - (i++ + 1))));
            
            reportStatus("processing " + urlForDate);
            
            try {
                fileUpdater.processUri(urlForDate);
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(urlForDate).withSource(getClass()));
            }
        }
    }

}
