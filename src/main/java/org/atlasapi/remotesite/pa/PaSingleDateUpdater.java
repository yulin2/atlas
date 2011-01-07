package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;

import com.google.common.collect.ImmutableSet;

@Controller
public class PaSingleDateUpdater extends PaBaseProgrammeUpdater {
    
    private final String dateString;
    private final PaLocalFileManager fileManager;

    public PaSingleDateUpdater(PaProgrammeProcessor processor, AdapterLog log, PaLocalFileManager fileManager, String dateString) {
        super(processor, log);
        this.fileManager = fileManager;
        this.dateString = dateString;
    }

    @Override
    public void run() {
        for (File file: fileManager.localFiles()) {
            if (file.getAbsolutePath().contains(dateString)) {
                processFiles(ImmutableSet.of(file));
            }
        }
    }
}
