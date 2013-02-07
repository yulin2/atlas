package org.atlasapi.query.v4.topic;


public interface QueryExecutor<T> {

    QueryResult<T> execute(Query<T> query) throws QueryExecutionException;
    
}
