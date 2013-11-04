package org.atlasapi.application;

import java.util.List;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.content.criteria.AttributeQuerySet;
import org.atlasapi.content.criteria.IdAttributeQuery;
import org.atlasapi.content.criteria.QueryVisitorAdapter;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.useraware.UserAwareQueryResult;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.useraware.UserAwareQuery;
import org.atlasapi.query.common.useraware.UserAwareQueryExecutor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SourcesQueryExecutor implements UserAwareQueryExecutor<Publisher> {
    private final SourceIdCodec sourceIdCodec;
    
    public SourcesQueryExecutor(SourceIdCodec sourceIdCodec) {
        this.sourceIdCodec = sourceIdCodec;
    }

    @Override
    public UserAwareQueryResult<Publisher> execute(UserAwareQuery<Publisher> query) throws QueryExecutionException {
        return query.isListQuery() ? multipleQuery(query) : singleQuery(query);
    }

    private UserAwareQueryResult<Publisher> singleQuery(UserAwareQuery<Publisher> query) throws NotFoundException {
        Optional<Publisher> source = sourceIdCodec.decode(query.getOnlyId());
        if (source.isPresent()) {
            return UserAwareQueryResult.singleResult(source.get(), query.getContext());
        } else {
            throw new NotFoundException(query.getOnlyId());
        }
    }

    private UserAwareQueryResult<Publisher> multipleQuery(UserAwareQuery<Publisher> query) throws NotFoundException {
        AttributeQuerySet operands = query.getOperands();
     
        Iterable<Publisher> requestedSources = Iterables.concat(operands.accept(new QueryVisitorAdapter<List<Publisher>>() {

            @Override
            public List<Publisher> visit(IdAttributeQuery query) {
               return Lists.transform(query.getValue(), new Function<Id, Publisher>() {

                @Override
                public Publisher apply(Id input) {
                    return sourceIdCodec.decode(input).get();
                }});
            }}));
   
        if (Iterables.isEmpty(requestedSources)) {
            return UserAwareQueryResult.listResult(Publisher.all(), query.getContext());
        } else {
            return UserAwareQueryResult.listResult(requestedSources, query.getContext());
        }
    }
    
    
}
