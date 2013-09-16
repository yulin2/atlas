package org.atlasapi.application;

import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;


public class SourcesQueryExecutor implements QueryExecutor<Publisher> {
    
    @Override
    public QueryResult<Publisher> execute(Query<Publisher> query) throws QueryExecutionException {
        return query.isListQuery() ? multipleQuery(query) : singleQuery(query);
    }

    private QueryResult<Publisher> singleQuery(Query<Publisher> query) {
        // TODO Auto-generated method stub
        return null;
    }

    private QueryResult<Publisher> multipleQuery(Query<Publisher> query) {
        // Iterables.filter
        return QueryResult.listResult(Publisher.all(), query.getContext());
    }
    
    

}
