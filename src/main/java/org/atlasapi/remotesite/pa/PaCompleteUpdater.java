package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;

import com.google.common.base.Predicates;

public class PaCompleteUpdater extends PaBaseProgrammeUpdater implements Runnable {
    
    private final PaProgrammeDataStore fileManager;

    public PaCompleteUpdater(PaProgDataProcessor processor, PaProgrammeDataStore fileManager, AdapterLog log) {
        super(processor, fileManager, log, "complete");
        this.fileManager = fileManager;
    }
    
    @Override
    public void runTask() {
        this.processFiles(fileManager.localFiles(Predicates.<File>alwaysTrue()));
    }
    
}
