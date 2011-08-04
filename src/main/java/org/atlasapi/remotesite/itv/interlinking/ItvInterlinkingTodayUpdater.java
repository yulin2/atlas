package org.atlasapi.remotesite.itv.interlinking;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;

public class ItvInterlinkingTodayUpdater extends ScheduledTask {
    
    private final ItvInterlinkingSingleFileUpdater fileUpdater;
    private final AdapterLog log;
    
    public ItvInterlinkingTodayUpdater(ItvInterlinkingSingleFileUpdater fileUpdater, AdapterLog log) {
        this.fileUpdater = fileUpdater;
        this.log = log;
    }
    
    @Override
    protected void runTask() {
        String url = fileUpdater.getUrlForDate(new SystemClock().now());
        try {
            fileUpdater.processUri(url);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(url).withSource(getClass()));
        }
    }
}
