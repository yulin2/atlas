package org.atlasapi.equiv.update;

import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.Publisher;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class NullContentEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    @Override
    public EquivalenceResult<T> updateEquivalences(T content, Optional<List<T>> externalCandidates) {
        return new EquivalenceResult<T>(content, ImmutableList.<ScoredEquivalents<T>>of(), DefaultScoredEquivalents.<T>fromSource("null").build(), 
                ImmutableMap.<Publisher, ScoredEquivalent<T>>of(), new DefaultDescription());
    }

}
