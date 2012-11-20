package org.atlasapi.equiv.query;

import java.util.List;
import java.util.Map;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;

public class MergeOnOutputQueryExecutor implements KnownTypeQueryExecutor {

    private final KnownTypeQueryExecutor delegate;
    private final OutputContentMerger outputContentMerger;

    public MergeOnOutputQueryExecutor(KnownTypeQueryExecutor delegate) {
        this.delegate = delegate;
        this.outputContentMerger = new OutputContentMerger();
    }

    @Override
    public Map<String, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
        return outputContentMerger.mergeResults(query.getConfiguration(), delegate.executeUriQuery(uris, query));
    }

    @Override
    public Map<String, List<Identified>> executeIdQuery(Iterable<Long> ids, final ContentQuery query) {
        return outputContentMerger.mergeResults(query.getConfiguration(), delegate.executeIdQuery(ids, query));
    }

}
