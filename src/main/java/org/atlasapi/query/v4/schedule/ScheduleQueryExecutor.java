package org.atlasapi.query.v4.schedule;

import org.atlasapi.media.entity.ChannelSchedule;

public interface ScheduleQueryExecutor {

    ChannelSchedule execute(ScheduleQuery scheduleQuery) throws ScheduleQueryExecutionException;

}
