package org.atlasapi.application.users;

import java.util.List;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.media.common.Id;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

public class UsersQueryExecutor implements QueryExecutor<User> {
    
    private final UserStore userStore;
    
    public UsersQueryExecutor(UserStore userStore) {
        this.userStore = userStore;
    }
    
    @Override
    public QueryResult<User> execute(Query<User> query) throws QueryExecutionException {
        return query.isListQuery() ? multipleQuery(query) : singleQuery(query);
    }
    
    private QueryResult<User> singleQuery(Query<User> query) throws NotFoundException {
        Id id = query.getOnlyId();
        Optional<User> user = userStore.userForId(id);
        if (user.isPresent()) {
            return QueryResult.singleResult(user.get(), query.getContext());
        } else {
            throw new NotFoundException(id);
        }
    }
    
    private QueryResult<User> multipleQuery(Query<User> query) throws NotFoundException {
        AttributeQuerySet operands = query.getOperands();
        
        Iterable<Id> ids = Iterables.concat(operands.accept(new QueryVisitorAdapter<List<Id>>() {
            @Override
             public List<Id> visit(IdAttributeQuery query) {
                 return query.getValue();
             }}));
        if (!Iterables.isEmpty(ids)) {
            return usersQueryForIds(query, ids);
        } else {
            return allUsersQuery(query);
        }
    
    }
    
    private QueryResult<User> usersQueryForIds(Query<User> query, Iterable<Id> ids) {
        return QueryResult.listResult(userStore.usersFor(ids), query.getContext());
    }
    
    private QueryResult<User> allUsersQuery(Query<User> query) {
        return QueryResult.listResult(userStore.allUsers(), query.getContext());
    }
}
