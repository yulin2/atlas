package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public class ResultWritingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final ContentEquivalenceUpdater<T> delegate;

    public ResultWritingEquivalenceUpdater(ContentEquivalenceUpdater<T> delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        EquivalenceResult<T> result = delegate.updateEquivalences(content);
        //TODO store result
        return result;
    }

}
