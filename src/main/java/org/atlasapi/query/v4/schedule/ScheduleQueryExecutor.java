package org.atlasapi.query.v4.schedule;

import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryResult;

public interface ScheduleQueryExecutor {

    QueryResult<ChannelSchedule> execute(ScheduleQuery scheduleQuery)
        throws QueryExecutionException;

}
