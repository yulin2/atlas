package org.atlasapi.remotesite.rovi.processing.restartable;

import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;

public interface IngestProcessingStep {

    IngestStep getStep();
    RoviDataProcessingResult execute();
    RoviDataProcessingResult execute(IngestStatus recoveredStatus);

}
