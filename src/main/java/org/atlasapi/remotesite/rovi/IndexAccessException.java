package org.atlasapi.remotesite.rovi;


/**
 * Exception thrown when there are issues when reading or writing the index
 *
 */
public class IndexAccessException extends Exception {

    private static final long serialVersionUID = 1L;

    public IndexAccessException() {
        super();
    }

    public IndexAccessException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IndexAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexAccessException(String message) {
        super(message);
    }

    public IndexAccessException(Throwable cause) {
        super(cause);
    }

}
