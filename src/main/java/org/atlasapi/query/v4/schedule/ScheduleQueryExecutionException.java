package org.atlasapi.query.v4.schedule;

public class ScheduleQueryExecutionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ScheduleQueryExecutionException() {
        super();
    }
    
    public ScheduleQueryExecutionException(String message) {
        super(message);
    }
    
    public ScheduleQueryExecutionException(Throwable cause) {
        super(cause);
    }

    public ScheduleQueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
