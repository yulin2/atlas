package org.atlasapi.application;

import java.util.List;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.elasticsearch.common.collect.Lists;

import com.google.common.base.Optional;

public class ApplicationQueryExecutor implements QueryExecutor<Application> {

    private final ApplicationStore applicationStore;

    public ApplicationQueryExecutor(ApplicationStore applicationStore) {
        this.applicationStore = applicationStore;
    }

    @Override
    public QueryResult<Application> execute(Query<Application> query)
            throws QueryExecutionException {
        return query.isListQuery() ? multipleQuery(query) : singleQuery(query);
    }

    private QueryResult<Application> singleQuery(Query<Application> query) throws NotFoundException {
        Id id = query.getOnlyId();
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            return QueryResult.singleResult(application.get(), query.getContext());
        } else {
            throw new NotFoundException(id);
        }

    }

    private QueryResult<Application> multipleQuery(Query<Application> query) {
        AttributeQuerySet operands = query.getOperands();
        List<Id> ids = Lists.newLinkedList();
        for (AttributeQuery<?> operand : operands) {
            if (operand instanceof org.atlasapi.content.criteria.IdAttributeQuery) {
                IdAttributeQuery idQuery = (IdAttributeQuery) operand;
                ids.addAll(idQuery.getValue());
            }
        }
        
        Iterable<Application> applications = null;
        if (ids.isEmpty()) {
            applications = applicationStore.allApplications();
        } else {
            applications = applicationStore.applicationsFor(ids);
        }
        return QueryResult.listResult(applications, query.getContext());
    }

}
