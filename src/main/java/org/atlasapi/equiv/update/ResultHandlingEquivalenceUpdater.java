package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Optional;

public class ResultHandlingEquivalenceUpdater<T extends Content> implements EquivalenceUpdater<T> {

    private final EquivalenceUpdater<T> delegate;
    private final EquivalenceResultHandler<T> handler;

    public ResultHandlingEquivalenceUpdater(EquivalenceUpdater<T> delegate, EquivalenceResultHandler<T> handler) {
        this.delegate = delegate;
        this.handler = handler;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content, Optional<List<T>> externalCandidates) {
        EquivalenceResult<T> result = delegate.updateEquivalences(content, externalCandidates);
        handler.handle(result);
        return result;
    }

}
