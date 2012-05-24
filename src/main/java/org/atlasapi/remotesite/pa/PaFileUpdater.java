package org.atlasapi.remotesite.pa;

import org.apache.log4j.Logger;

import com.metabroadcast.common.scheduling.ScheduledTask;

public class PaFileUpdater extends ScheduledTask {
    
    private static final Logger log = Logger.getLogger(PaFileUpdater.class);
    private final PaFtpFileUpdater fileManager;

    public PaFileUpdater(PaFtpFileUpdater fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void runTask() {
        try {
            fileManager.updateFilesFromFtpSite();
        } catch (Exception e) {
            log.error("Error when updating files from the PA FTP site", e);
        }
    }
}
