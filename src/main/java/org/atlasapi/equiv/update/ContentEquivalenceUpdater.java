package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.content.Content;

import com.google.common.base.Optional;

//TODO: this should return void: system update equivalence for content.
public interface ContentEquivalenceUpdater<T extends Content> {

    EquivalenceResult<T> updateEquivalences(T content, Optional<List<T>> externalCandidates);
    
}
