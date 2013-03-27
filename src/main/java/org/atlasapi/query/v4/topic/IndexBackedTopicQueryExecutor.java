package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicIndex;
import org.atlasapi.media.topic.TopicResolver;
import org.atlasapi.media.util.Resolved;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryResult;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metabroadcast.common.query.Selection;

public class IndexBackedTopicQueryExecutor implements QueryExecutor<Topic> {

    private final TopicIndex index;
    private final TopicResolver resolver;

    public IndexBackedTopicQueryExecutor(TopicIndex index, TopicResolver resolver) {
        this.index = checkNotNull(index);
        this.resolver = checkNotNull(resolver);
    }

    @Override
    public QueryResult<Topic> execute(Query<Topic> query) throws QueryExecutionException {
        return resultFor(Futures.get(resolve(getTopicIds(query)),
            1, TimeUnit.MINUTES, QueryExecutionException.class), query);
    }

    private QueryResult<Topic> resultFor(Resolved<Topic> resolved, Query<Topic> query) {
        return query.isListQuery() ? listResult(resolved, query)
                                   : singleResult(resolved, query);
    }

    private QueryResult<Topic> singleResult(Resolved<Topic> resolved, Query<Topic> query) {
        return QueryResult.singleResult(Iterables.getOnlyElement(resolved.getResources()), query.getContext());
    }

    private QueryResult<Topic> listResult(Resolved<Topic> resolved, Query<Topic> query) {
        return QueryResult.listResult(resolved.getResources(), query.getContext());
    }

    private ListenableFuture<FluentIterable<Id>> getTopicIds(Query<Topic> query)
            throws QueryExecutionException {
        return query.isListQuery() ? queryIndex(query)
                                   : Futures.immediateFuture(FluentIterable.from(ImmutableList.of(query.getOnlyId())));
    }

    private ListenableFuture<FluentIterable<Id>> queryIndex(Query<Topic> query)
            throws QueryExecutionException {
        return index.query(
            query.getOperands(), 
            query.getContext().getApplicationConfiguration().getEnabledSources(), 
            query.getContext().getSelection().or(Selection.ALL)
        );
    }

    private ListenableFuture<Resolved<Topic>> resolve(ListenableFuture<FluentIterable<Id>> ids) {
        return Futures.transform(ids, new AsyncFunction<FluentIterable<Id>, Resolved<Topic>>() {

            @Override
            public ListenableFuture<Resolved<Topic>> apply(FluentIterable<Id> ids)
                    throws Exception {
                return resolver.resolveIds(ids);
            }
        });
    }

}
