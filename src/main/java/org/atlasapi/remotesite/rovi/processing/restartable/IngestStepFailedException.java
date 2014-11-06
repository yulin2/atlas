package org.atlasapi.remotesite.rovi.processing.restartable;

public class IngestStepFailedException extends RuntimeException {

    public IngestStepFailedException() {
    }

    public IngestStepFailedException(String message) {
        super(message);
    }

    public IngestStepFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public IngestStepFailedException(Throwable cause) {
        super(cause);
    }
}
