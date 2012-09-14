package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

public class NothingEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    @Override
    public Maybe<ScoredCandidate<T>> extract(T target, List<ScoredCandidate<T>> equivalents, ResultDescription desc) {
        return Maybe.nothing();
    }

}
