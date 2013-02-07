package org.atlasapi.query.v4.schedule;

import java.io.IOException;

import org.atlasapi.query.common.QueryResult;

public interface QueryResultWriter<T> {

    void write(QueryResult<T> result, ResponseWriter responseWriter) throws IOException;

}