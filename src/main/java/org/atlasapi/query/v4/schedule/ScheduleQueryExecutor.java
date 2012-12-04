package org.atlasapi.query.v4.schedule;


public interface ScheduleQueryExecutor {

    ScheduleQueryResult execute(ScheduleQuery scheduleQuery) throws ScheduleQueryExecutionException;

}
