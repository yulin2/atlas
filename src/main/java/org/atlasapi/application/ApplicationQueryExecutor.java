package org.atlasapi.application;

import java.util.List;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.useraware.UserAwareQueryResult;
import org.atlasapi.persistence.application.ApplicationStore;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.useraware.UserAwareQuery;
import org.atlasapi.query.common.useraware.UserAwareQueryExecutor;
import org.atlasapi.content.criteria.AttributeQuery;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.content.criteria.attribute.Attributes;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class ApplicationQueryExecutor implements UserAwareQueryExecutor<Application> {

    private final ApplicationStore applicationStore;

    public ApplicationQueryExecutor(ApplicationStore applicationStore) {
        this.applicationStore = applicationStore;
    }

    @Override
    public UserAwareQueryResult<Application> execute(UserAwareQuery<Application> query)
            throws QueryExecutionException {
        return query.isListQuery() ? multipleQuery(query) : singleQuery(query);
    }

    private UserAwareQueryResult<Application> singleQuery(UserAwareQuery<Application> query) throws NotFoundException {
        Id id = query.getOnlyId();
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            return UserAwareQueryResult.singleResult(application.get(), query.getContext());
        } else {
            throw new NotFoundException(id);
        }

    }

    private UserAwareQueryResult<Application> multipleQuery(UserAwareQuery<Application> query) {
        AttributeQuerySet operands = query.getOperands();

        Iterable<Id> ids = Iterables.concat(operands.accept(new QueryVisitorAdapter<List<Id>>() {
           @Override
            public List<Id> visit(IdAttributeQuery query) {
                return query.getValue();
            }}));
        Publisher reads = null;
            
        Publisher writes = null;

        for (AttributeQuery<?> operand : operands) {
             if (operand.getAttributeName().equals(Attributes.SOURCE_READS.externalName())) {
                reads = (Publisher) Iterables.getOnlyElement(operand.getValue());
            } else if (operand.getAttributeName().equals(Attributes.SOURCE_WRITES.externalName())) {
                writes = (Publisher) Iterables.getOnlyElement(operand.getValue());
            }
        }
        if (!Iterables.isEmpty(ids)) {
            return applicationsQueryForIds(query, ids);
        } else if (reads != null) {
            return applicationsReading(query, reads);
        } else if (writes != null) {
            return applicationsWriting(query, writes);
        } else {
            return allApplicationsQuery(query);
        }
    }
    
    private UserAwareQueryResult<Application> applicationsQueryForIds(UserAwareQuery<Application> query, Iterable<Id> ids) {
        return UserAwareQueryResult.listResult(applicationStore.applicationsFor(ids), query.getContext());
    }
    
    private UserAwareQueryResult<Application> allApplicationsQuery(UserAwareQuery<Application> query) {
        return UserAwareQueryResult.listResult(applicationStore.allApplications(), query.getContext());
    }
    
    private UserAwareQueryResult<Application> applicationsReading(UserAwareQuery<Application> query, Publisher source) {
        return UserAwareQueryResult.listResult(applicationStore.readersFor(source), query.getContext());
    }
    
    private UserAwareQueryResult<Application> applicationsWriting(UserAwareQuery<Application> query, Publisher source) {
        return UserAwareQueryResult.listResult(applicationStore.writersFor(source), query.getContext());
    }
}
