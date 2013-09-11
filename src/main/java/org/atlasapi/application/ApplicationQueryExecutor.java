package org.atlasapi.application;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.content.criteria.AttributeQuerySet;

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
        Iterable<Application> applications = applicationStore.allApplications();
        
//        Set<Application2> applications = Sets.newHashSet();
//        Application2 application = Application2.builder()
//                .withId("dfg")
//                .withTitle("test result")
//                .build();
//        applications.add(application);
        return QueryResult.listResult(applications, query.getContext());
    }

}
