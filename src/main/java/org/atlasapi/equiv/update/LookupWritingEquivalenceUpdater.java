package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

public class LookupWritingEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    @Override
    public EquivalenceResult<T> updateEquivalences(T content) {
        return null;
    }

}
