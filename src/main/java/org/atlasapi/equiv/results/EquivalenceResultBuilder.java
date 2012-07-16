package org.atlasapi.equiv.results;

import java.util.List;

import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

public interface EquivalenceResultBuilder<T extends Content> {

    EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents, ReadableDescription desc);

}