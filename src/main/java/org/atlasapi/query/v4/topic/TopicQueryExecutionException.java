package org.atlasapi.query.v4.topic;

public class TopicQueryExecutionException extends Exception {

    private static final long serialVersionUID = 1L;

    public TopicQueryExecutionException() {
        super();
    }
    
    public TopicQueryExecutionException(String message) {
        super(message);
    }
    
    public TopicQueryExecutionException(Throwable cause) {
        super(cause);
    }

    public TopicQueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
