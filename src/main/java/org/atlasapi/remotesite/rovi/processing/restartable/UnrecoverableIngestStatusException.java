package org.atlasapi.remotesite.rovi.processing.restartable;

public class UnrecoverableIngestStatusException extends RuntimeException {

    public UnrecoverableIngestStatusException() {
    }

    public UnrecoverableIngestStatusException(String message) {
        super(message);
    }

    public UnrecoverableIngestStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecoverableIngestStatusException(Throwable cause) {
        super(cause);
    }

}
