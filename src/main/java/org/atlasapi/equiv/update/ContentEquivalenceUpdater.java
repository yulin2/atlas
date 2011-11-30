package org.atlasapi.equiv.update;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.entity.Content;

//TODO: this should return void: system update equivalence for content.
public interface ContentEquivalenceUpdater<T extends Content> {

    EquivalenceResult<T> updateEquivalences(T content);
    
}
