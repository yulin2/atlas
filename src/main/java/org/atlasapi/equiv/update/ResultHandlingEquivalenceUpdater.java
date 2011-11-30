package org.atlasapi.equiv.update;

import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public class ResultHandlingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final ContentEquivalenceUpdater<T> delegate;
    private final EquivalenceResultHandler<T> handler;

    public ResultHandlingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate, EquivalenceResultHandler<T> handler) {
        this.delegate = delegate;
        this.handler = handler;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        EquivalenceResult<T> result = delegate.updateEquivalences(content);
        handler.handle(result);
        return result;
    }

}
