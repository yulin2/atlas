package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.PaChannelProcessJob.PaChannelProcessJobBuilder;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.springframework.stereotype.Controller;

import com.google.common.base.Predicate;

@Controller
public class PaSingleDateUpdater extends PaBaseProgrammeUpdater {
    
    private final String dateString;
    private final PaProgrammeDataStore fileManager;

    public PaSingleDateUpdater(PaChannelProcessJobBuilder jobBuilder, AdapterLog log, PaProgrammeDataStore fileManager, String dateString) {
        super(jobBuilder, fileManager, log);
        this.fileManager = fileManager;
        this.dateString = dateString;
    }

    @Override
    public void runTask() {
        processFiles(fileManager.localFiles(new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return input.getName().contains(dateString);
            }
        }));
    }
}
