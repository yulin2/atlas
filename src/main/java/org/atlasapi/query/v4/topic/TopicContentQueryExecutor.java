package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentIndex;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicResolver;
import org.atlasapi.media.util.Resolved;
import org.atlasapi.query.common.ContextualQuery;
import org.atlasapi.query.common.ContextualQueryExecutor;
import org.atlasapi.query.common.ContextualQueryResult;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryResult;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metabroadcast.common.query.Selection;

public class TopicContentQueryExecutor implements ContextualQueryExecutor<Topic, Content> {

    private static final long QUERY_TIMEOUT = 60000;

    private final TopicResolver topicResolver;
    private final ContentIndex index;
    private final ContentResolver contentResolver;

    public TopicContentQueryExecutor(TopicResolver topicResolver, ContentIndex index,
        ContentResolver contentResolver) {
        this.topicResolver = checkNotNull(topicResolver);
        this.index = checkNotNull(index);
        this.contentResolver = checkNotNull(contentResolver);
    }

    @Override
    public ContextualQueryResult<Topic, Content> execute(final ContextualQuery<Topic, Content> query)
        throws QueryExecutionException {
        return Futures.get(Futures.transform(
                resolveTopic(query.getContextQuery().getOnlyId()), resolveContentToContextualQuery(query)
            ), QUERY_TIMEOUT, MILLISECONDS, QueryExecutionException.class);
    }

    private AsyncFunction<Resolved<Topic>, ContextualQueryResult<Topic, Content>> resolveContentToContextualQuery(
            final ContextualQuery<Topic, Content> query) {
        return new AsyncFunction<Resolved<Topic>, ContextualQueryResult<Topic, Content>>(){

            @Override
            public ListenableFuture<ContextualQueryResult<Topic, Content>> apply(
                    Resolved<Topic> resolved) throws Exception {
        
                    Optional<Topic> possibleTopic = resolved.getResources().first();
            
                    if (!possibleTopic.isPresent()) {
                        throw new QueryExecutionException();
                    }
            
                    final Topic topic = possibleTopic.get();
            
                    final QueryContext context = query.getContext();
                    if (!context.getApplicationConfiguration().isEnabled(topic.getPublisher())) {
                        throw new QueryExecutionException();
                    }
        
                    return Futures.transform(resolveContent(queryIndex(query.getResourceQuery())), 
                            toContextualQuery(topic, context));
                }

            };
    }
    
    private Function<Resolved<Content>, ContextualQueryResult<Topic, Content>> toContextualQuery(
            final Topic topic, final QueryContext context) {
        return new Function<Resolved<Content>, ContextualQueryResult<Topic, Content>>() {
            @Override
            public ContextualQueryResult<Topic, Content> apply(Resolved<Content> content) {
                return ContextualQueryResult.valueOf(
                        QueryResult.singleResult(topic, context),
                        QueryResult.listResult(content.getResources(), context),
                        context);
            }

        };
    }

    private ListenableFuture<Resolved<Content>> resolveContent(
            ListenableFuture<FluentIterable<Id>> queryHits) {
        return Futures.transform(queryHits,
                new AsyncFunction<FluentIterable<Id>, Resolved<Content>>() {
                    @Override
                    public ListenableFuture<Resolved<Content>> apply(FluentIterable<Id> ids)
                            throws Exception {
                        return contentResolver.resolveIds(ids);
                    }
                });
    }
    
    private ListenableFuture<Resolved<Topic>> resolveTopic(Id contextId) {
        return topicResolver.resolveIds(ImmutableList.of(contextId));
    }
    
    private ListenableFuture<FluentIterable<Id>> queryIndex(Query<Content> query) throws QueryExecutionException {
        return index.query(
            query.getOperands(), 
            query.getContext().getApplicationConfiguration().getEnabledSources(), 
            query.getContext().getSelection().or(Selection.ALL)
        );
    }

}
