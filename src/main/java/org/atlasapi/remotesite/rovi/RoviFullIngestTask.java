package org.atlasapi.remotesite.rovi;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.ScheduledTask;


public class RoviFullIngestTask extends ScheduledTask {
    
    private final static Logger LOG = LoggerFactory.getLogger(RoviUpdater.class); 

    private final RoviFullIngestProcessor fullIngestProcessor;
    private final File programsFile;
    private final File seasonsFile;
    private final File scheduleFile;
    
    public RoviFullIngestTask(RoviFullIngestProcessor fullIngestProcessor, File programsFile, File seriesFile, File scheduleFile) {
        this.fullIngestProcessor = fullIngestProcessor;
        this.programsFile = programsFile;
        this.seasonsFile = seriesFile;
        this.scheduleFile = scheduleFile;
    }
    
    @Override
    protected void runTask() {
        try {
            fullIngestProcessor.process(programsFile, seasonsFile, scheduleFile);
        } catch (IOException e) {
            LOG.error("Error while processing programs", e);
        }
        
    }
}
