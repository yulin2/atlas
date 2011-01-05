package org.atlasapi.remotesite.pa;

import org.atlasapi.persistence.logging.AdapterLog;

public class PaCompleteUpdater extends PaBaseProgrammeUpdater implements Runnable {
    private final PaLocalFileManager fileManager;

    public PaCompleteUpdater(PaProgrammeProcessor processor, PaLocalFileManager fileManager, AdapterLog log) {
        super(processor, log);
        this.fileManager = fileManager;
    }
    
    @Override
    public void run() {
        this.processFiles(fileManager.localFiles());
    }
}
