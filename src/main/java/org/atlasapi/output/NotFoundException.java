package org.atlasapi.output;

import org.atlasapi.query.common.QueryExecutionException;

public class NotFoundException extends QueryExecutionException {

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

}
