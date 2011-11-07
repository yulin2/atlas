package org.atlasapi.remotesite.pa;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

import com.metabroadcast.common.scheduling.ScheduledTask;

public class PaFileUpdater extends ScheduledTask {
    
    private final PaFtpFileUpdater fileManager;
    private final AdapterLog log;

    public PaFileUpdater(PaFtpFileUpdater fileManager, AdapterLog log) {
        this.fileManager = fileManager;
        this.log = log;
    }

    @Override
    public void runTask() {
        try {
            fileManager.updateFilesFromFtpSite();
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("Error when updating files from the PA FTP site").withSource(PaFileUpdater.class));
        }
    }
}
