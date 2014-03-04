package org.atlasapi.remotesite.rovi.processing;

import java.io.File;
import java.io.IOException;


public interface RoviIngestProcessor {

    void process(File programFile, File seasonsFile, File scheduleFile,
            File programDescriptionsFile, File episodeSequenceFile) throws IOException ;
    
}
