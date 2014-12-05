package org.atlasapi.remotesite.rovi.processing;

import java.io.File;
import java.io.IOException;

public interface FileProcessor {

    RoviDataProcessingResult process(File file) throws IOException;

}
