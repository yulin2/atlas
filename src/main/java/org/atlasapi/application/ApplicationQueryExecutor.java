package org.atlasapi.application;

import java.util.List;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.elasticsearch.common.collect.Iterables;
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
        Publisher reads = null;
        Publisher writes = null;
       
        for (AttributeQuery<?> operand : operands) {
            if (operand.getAttributeName().equals(Attributes.ID.externalName())) {
                IdAttributeQuery idQuery = (IdAttributeQuery) operand;
                ids.addAll(idQuery.getValue());
            } else if (operand.getAttributeName().equals(Attributes.SOURCE_READS.externalName())) {
                reads = (Publisher) Iterables.getOnlyElement(operand.getValue());
            } else if (operand.getAttributeName().equals(Attributes.SOURCE_WRITES.externalName())) {
                writes = (Publisher) Iterables.getOnlyElement(operand.getValue());
            }
        }
        if (!ids.isEmpty()) {
            return applicationsQueryForIds(query, ids);
        } else if (reads != null) {
            return applicationsReading(query, reads);
        } else if (writes != null) {
            return applicationsWriting(query, writes);
        } else {
            return allApplicationsQuery(query);
        }
    }
    
    private QueryResult<Application> applicationsQueryForIds(Query<Application> query, Iterable<Id> ids) {
        return QueryResult.listResult(applicationStore.applicationsFor(ids), query.getContext());
    }
    
    private QueryResult<Application> allApplicationsQuery(Query<Application> query) {
        return QueryResult.listResult(applicationStore.allApplications(), query.getContext());
    }
    
    private QueryResult<Application> applicationsReading(Query<Application> query, Publisher source) {
        return QueryResult.listResult(applicationStore.readersFor(source), query.getContext());
    }
    
    private QueryResult<Application> applicationsWriting(Query<Application> query, Publisher source) {
        return QueryResult.listResult(applicationStore.writersFor(source), query.getContext());
    }
}
