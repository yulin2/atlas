package org.atlasapi.remotesite.rovi.tasks;

import java.io.File;
import java.io.IOException;

import org.atlasapi.remotesite.rovi.processing.RoviIngestProcessor;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class RoviIngestTask extends ScheduledTask {

    private final RoviIngestProcessor ingestProcessor;
    private final File programsFile;
    private final File seasonsFile;
    private final File scheduleFile;
    private final File programDescriptionsFile;
    private final File episodeSequenceFile;
    
    public RoviIngestTask(RoviIngestProcessor ingestProcessor, File programsFile,
            File seasonsFile, File scheduleFile, File programDescriptionsFile,
            File episodeSequenceFile) {
        this.ingestProcessor = ingestProcessor;
        this.programsFile = programsFile;
        this.seasonsFile = seasonsFile;
        this.scheduleFile = scheduleFile;
        this.programDescriptionsFile = programDescriptionsFile;
        this.episodeSequenceFile = episodeSequenceFile;
    }
    
    @Override
    protected void runTask() {
        try {
            ingestProcessor.process(programsFile,
                    seasonsFile,
                    scheduleFile,
                    programDescriptionsFile,
                    episodeSequenceFile);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
