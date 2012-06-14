package org.atlasapi.remotesite.pa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.ScheduledTask;

public class PaFileUpdater extends ScheduledTask {
    
    private static final Logger log = LoggerFactory.getLogger(PaFileUpdater.class);
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
