package org.atlasapi.application;

import java.util.List;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.EnumAttributeQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;
import com.google.common.collect.Iterables;


public class SourceRequestQueryExecutor implements QueryExecutor<SourceRequest> {
    private final SourceRequestStore requestStore;
    private static final QueryVisitorAdapter<Publisher> PUBLISHERS_VISITOR = new QueryVisitorAdapter<Publisher>(){
      
        @Override
        public Publisher visit(EnumAttributeQuery<?> query) {
            if (query.getAttributeName().equals(Attributes.SOURCE_REQUEST_SOURCE.externalName())) {
                return (Publisher) Iterables.getOnlyElement(query.getValue());
            } else {
                return null;
            }
        }
     };
    
    public SourceRequestQueryExecutor(SourceRequestStore requestStore) {
        this.requestStore = requestStore;
    }

    @Override
    public QueryResult<SourceRequest> execute(Query<SourceRequest> query)
            throws QueryExecutionException {
        AttributeQuerySet operands = query.getOperands();

        List<Publisher> source = operands.accept(PUBLISHERS_VISITOR);
        
        if (source.isEmpty()) {
            return QueryResult.listResult(requestStore.all(), query.getContext());
        } else {
            return QueryResult.listResult(requestStore.sourceRequestsFor(source.get(0)), query.getContext());            
        }
    }

}
