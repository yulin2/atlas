package org.atlasapi.application;

import org.atlasapi.application.model.SourceRequest;
import org.atlasapi.application.persistence.SourceRequestStore;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;
import org.elasticsearch.common.collect.Iterables;


public class SourceRequestQueryExecutor implements QueryExecutor<SourceRequest> {
    private final SourceRequestStore requestStore;
    
    public SourceRequestQueryExecutor(SourceRequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public QueryResult<SourceRequest> execute(Query<SourceRequest> query)
            throws QueryExecutionException {
        AttributeQuerySet operands = query.getOperands();

        Publisher source = null;
       
        for (AttributeQuery<?> operand : operands) {
             if (operand.getAttributeName().equals(Attributes.SOURCE_REQUEST_SOURCE.externalName())) {
                source = (Publisher) Iterables.getOnlyElement(operand.getValue());
            } 
        }
        if (source != null) {
            return QueryResult.listResult(requestStore.sourceRequestsFor(source), query.getContext());
        } else {
            return QueryResult.listResult(requestStore.all(), query.getContext());
        }
    }

}
