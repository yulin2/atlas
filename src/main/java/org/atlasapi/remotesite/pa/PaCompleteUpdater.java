package org.atlasapi.remotesite.pa;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;

public class PaCompleteUpdater extends PaBaseProgrammeUpdater implements Runnable {
    
    private final DefaultPaProgrammeDataStore fileManager;

    public PaCompleteUpdater(PaProgrammeProcessor processor, DefaultPaProgrammeDataStore fileManager, AdapterLog log) {
        super(processor, log);
        this.fileManager = fileManager;
    }
    
    @Override
    public void run() {
        this.processFiles(fileManager.localFiles());
    }
    
}
