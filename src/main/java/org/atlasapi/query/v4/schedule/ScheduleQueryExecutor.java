package org.atlasapi.query.v4.schedule;

import org.atlasapi.media.entity.Schedule.ScheduleChannel;

public interface ScheduleQueryExecutor {

    ScheduleChannel execute(ScheduleQuery scheduleQuery) throws ScheduleQueryExecutionException;

}
