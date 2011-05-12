package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.media.entity.Content;

public class ResultWritingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final ContentEquivalenceUpdater<T> delegate;
    private final EquivalenceResultStore store;

    public ResultWritingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate, EquivalenceResultStore store) {
        this.delegate = delegate;
        this.store = store;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        EquivalenceResult<T> result = delegate.updateEquivalences(content);
        store.store(result);
        return result;
    }

}
