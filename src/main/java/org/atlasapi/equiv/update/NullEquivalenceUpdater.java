package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class NullEquivalenceUpdater<T> implements EquivalenceUpdater<T> {
    
    public static final <T> EquivalenceUpdater<T> get() {
        return new NullEquivalenceUpdater<T>();
    }

    private NullEquivalenceUpdater() {}
    
    @Override
    public EquivalenceResult<T> updateEquivalences(T content, Optional<List<T>> externalCandidates) {
        return new EquivalenceResult<T>(content, ImmutableList.<ScoredCandidates<T>>of(), DefaultScoredEquivalents.<T>fromSource("null").build(), 
                ImmutableMap.<Publisher, ScoredCandidate<T>>of(), new DefaultDescription());
    }

}
