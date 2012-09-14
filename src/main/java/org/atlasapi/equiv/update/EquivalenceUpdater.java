package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;

import com.google.common.base.Optional;

//TODO: this should return void: system update equivalence for content.
public interface EquivalenceUpdater<T> {

    EquivalenceResult<T> updateEquivalences(T content, Optional<List<T>> externalCandidates);
    
}
