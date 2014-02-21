package org.atlasapi.remotesite.rovi.tasks;

import java.io.File;
import java.io.IOException;

import org.atlasapi.remotesite.rovi.processing.RoviDeltaIngestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.ScheduledTask;


public class RoviDeltaIngestTask extends ScheduledTask{

    private final static Logger LOG = LoggerFactory.getLogger(RoviDeltaIngestTask.class); 

    private final RoviDeltaIngestProcessor deltaIngestProcessor;
    private final File programsFile;
    private final File seasonsFile;
    private final File scheduleFile;
    
    public RoviDeltaIngestTask(RoviDeltaIngestProcessor deltaIngestProcessor, File programsFile, File seriesFile, File scheduleFile) {
        this.deltaIngestProcessor = deltaIngestProcessor;
        this.programsFile = programsFile;
        this.seasonsFile = seriesFile;
        this.scheduleFile = scheduleFile;
    }
    
    @Override
    protected void runTask() {
        try {
            deltaIngestProcessor.process(programsFile, seasonsFile, scheduleFile);
        } catch (IOException e) {
            LOG.error("Error while processing delta files", e);
        }
        
    }
}
