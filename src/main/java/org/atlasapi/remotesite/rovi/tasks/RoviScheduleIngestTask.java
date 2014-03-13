package org.atlasapi.remotesite.rovi.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.atlasapi.remotesite.rovi.processing.ScheduleFileProcessor;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class RoviScheduleIngestTask extends ScheduledTask {
    
    private final ScheduleFileProcessor processor;
    private final File scheduleFile;
    
    public RoviScheduleIngestTask(ScheduleFileProcessor processor, File scheduleFile) {
        this.processor = checkNotNull(processor);
        this.scheduleFile = checkNotNull(scheduleFile);
    }

    @Override
    protected void runTask() {
        try {
            processor.process(scheduleFile);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
    
}
