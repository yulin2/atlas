package org.atlasapi.application;

import org.atlasapi.application.model.SourceRequest;
import org.atlasapi.application.persistence.SourceRequestStore;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;


public class SourceRequestQueryExecutor implements QueryExecutor<SourceRequest> {
    private final SourceRequestStore requestStore;
    
    public SourceRequestQueryExecutor(SourceRequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public QueryResult<SourceRequest> execute(Query<SourceRequest> query)
            throws QueryExecutionException {
        // TODO Auto-generated method stub
        return QueryResult.listResult(requestStore.all(), query.getContext());
    }

}
