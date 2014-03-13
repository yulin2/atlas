package org.atlasapi.remotesite.rovi.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.atlasapi.remotesite.rovi.processing.ScheduleFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class RoviScheduleIngestTask extends ScheduledTask {
    
    private static final Logger LOG = LoggerFactory.getLogger(RoviScheduleIngestTask.class);
    
    private final ScheduleFileProcessor processor;
    private final File scheduleFolder;
    
    
    public RoviScheduleIngestTask(ScheduleFileProcessor processor, File scheduleFolder) {
        this.processor = checkNotNull(processor);
        this.scheduleFolder = checkNotNull(scheduleFolder);
    }

    @Override
    protected void runTask() {
        try {
            File[] files = scheduleFolder.listFiles();
            List<File> sortedFiles = Ordering.natural().sortedCopy(Arrays.asList(files));

            LOG.info("Starting only broadcasts ingest");
            
            for (File file: sortedFiles) {
                LOG.info("Going to ingest file {}", file.getName());
                process(file);
                LOG.info("Completed intest of file {}", file.getName());
            }
            
            LOG.info("Broadcasts ingest completed");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void process(File file) throws IOException {
        try {
            processor.process(file);
        } catch (Exception e) {
            LOG.error("Error while processing file " + file.getName(), e);
        }
    }
    
}
