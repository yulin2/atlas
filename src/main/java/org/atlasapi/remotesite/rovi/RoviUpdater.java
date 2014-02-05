package org.atlasapi.remotesite.rovi;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metabroadcast.common.scheduling.ScheduledTask;


public class RoviUpdater extends ScheduledTask {
    
    private final static Logger LOG = LoggerFactory.getLogger(RoviUpdater.class); 

    private final RoviProgramsProcessor programsProcessor;
    private final File programsFile;
    
    public RoviUpdater(RoviProgramsProcessor programsProcessor, File programsFile) {
        this.programsProcessor = programsProcessor;
        this.programsFile = programsFile;
    }
    
    @Override
    protected void runTask() {
        try {
            programsProcessor.process(programsFile);
        } catch (IOException e) {
            LOG.error("Error while processing programs", e);
        }
        
    }

}
