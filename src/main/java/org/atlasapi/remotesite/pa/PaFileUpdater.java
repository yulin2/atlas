package org.atlasapi.remotesite.pa;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

public class PaFileUpdater implements Runnable {
    
    private final PaFtpFileUpdater fileManager;
    private final AdapterLog log;
    private boolean isRunning = false;
    
    public boolean isRunning() {
        return isRunning;
    }

    public PaFileUpdater(PaFtpFileUpdater fileManager, AdapterLog log) {
        this.fileManager = fileManager;
        this.log = log;
    }

    @Override
    public void run() {
        if (isRunning) {
            throw new IllegalStateException("Already running");
        }

        isRunning = true;
        try {
            fileManager.updateFilesFromFtpSite();
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withDescription("Error when updating files from the PA FTP site").withSource(PaFileUpdater.class));
        } finally {
            isRunning = false;
        }
    }
}
