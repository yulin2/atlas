package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import com.google.common.base.Predicate;
import com.sun.tools.javac.util.List;
/*
public class PaUnprocessedFilesUpdater extends PaBaseProgrammeUpdater implements Runnable {

	private final PaProgrammeDataStore fileManager;
	private List<String> processedFiles;  // TODO: Make the processedFiles list mongo-backed
	
    public PaUnprocessedFilesUpdater(PaChannelProcessor channelProcessor, PaProgrammeDataStore fileManager, AdapterLog log) {
        super(channelProcessor, fileManager, log);
        this.fileManager = fileManager;
    }
    
    @Override
    public void runTask() {
        this.processFiles(fileManager.localFiles(new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return !processedFiles.contains(input.getName());
            }
        }));
    }
    
    @Override
    public void submittedForProcessing(File file) {
    	processedFiles.add(file.getName());
    }

}*/
