package org.atlasapi.remotesite.rovi.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

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
    private final String name;
    
    public RoviIngestTask(RoviIngestProcessor ingestProcessor, File programsFile,
            File seasonsFile, File scheduleFile, File programDescriptionsFile,
            File episodeSequenceFile, String name) {
        this.ingestProcessor = checkNotNull(ingestProcessor);
        this.programsFile = checkNotNull(programsFile);
        this.seasonsFile = checkNotNull(seasonsFile);
        this.scheduleFile = checkNotNull(scheduleFile);
        this.programDescriptionsFile = checkNotNull(programDescriptionsFile);
        this.episodeSequenceFile = checkNotNull(episodeSequenceFile);
        this.name = checkNotNull(name);
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
    
    @Override
    public String getName() {
        return name;
    }

}
