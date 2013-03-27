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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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
    public ContextualQueryResult<Topic, Content> execute(ContextualQuery<Topic, Content> query)
        throws QueryExecutionException {
        QueryContext context = query.getContext();

        Resolved<Topic> resolved = topicResolver.resolveIds(ImmutableList.of(query.getContextQuery()
            .getOnlyId()));
        Optional<Topic> possibleTopic = resolved.getResources().first();

        if (!possibleTopic.isPresent()) {
            throw new QueryExecutionException();
        }

        Topic topic = possibleTopic.get();

        if (!context.getApplicationConfiguration().isEnabled(topic.getPublisher())) {
            throw new QueryExecutionException();
        }
        
        ListenableFuture<Resolved<Content>> futureContent = 
            contentResolver.resolveIds(queryIndex(query.getResourceQuery()));

        Resolved<Content> content = 
            Futures.get(futureContent, QUERY_TIMEOUT, MILLISECONDS, QueryExecutionException.class);
        
        return ContextualQueryResult.valueOf(
            QueryResult.singleResult(topic, context), 
            QueryResult.listResult(content.getResources(), context), 
            context
        );
    }
    
    private Iterable<Id> queryIndex(Query<Content> query) throws QueryExecutionException {
        return Futures.get(index.query(
            query.getOperands(), 
            query.getContext().getApplicationConfiguration().getEnabledSources(), 
            query.getContext().getSelection().or(Selection.ALL)
        ), 1, TimeUnit.MINUTES, QueryExecutionException.class);
    }

}
