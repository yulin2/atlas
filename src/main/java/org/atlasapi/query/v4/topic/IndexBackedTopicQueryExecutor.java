package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.media.topic.TopicIndex;
import org.atlasapi.media.topic.TopicResolver;
import org.atlasapi.media.util.Resolved;

import com.google.common.base.Optional;

public class IndexBackedTopicQueryExecutor implements QueryExecutor<Topic> {

    private final TopicIndex index;
    private final TopicResolver resolver;

    public IndexBackedTopicQueryExecutor(TopicIndex index, TopicResolver resolver) {
        this.index = checkNotNull(index);
        this.resolver = checkNotNull(resolver);
    }
    
    @Override
    public QueryResult<Topic> execute(Query<Topic> query) throws QueryExecutionException {
        return resultFor(resolve(getTopicIds(query)), query);
    }

    private TopicQueryResult resultFor(Resolved<Topic> resolved, TopicQuery query) {
        return new TopicQueryResult(
            query.getSelection().apply(resolved.getResources().filter(query.asSourceFilter())), 
            query.getAnnotations(), query.getApplicationConfiguration());
    }

    private Iterable<Id> getTopicIds(TopicQuery query) {
        Optional<List<Id>> possibleIds = query.getIdsIfOnly();
        return possibleIds.isPresent() ? possibleIds.get()
                                       : queryIndex(query);
    }

    private Iterable<Id> queryIndex(TopicQuery query) {
        throw new UnsupportedOperationException();
    }
    
    private Resolved<Topic> resolve(Iterable<Id> ids) {
        return resolver.resolveIds(ids);
    }

}
