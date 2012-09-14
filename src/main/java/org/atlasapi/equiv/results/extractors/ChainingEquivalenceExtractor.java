package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

public abstract class ChainingEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    private final EquivalenceExtractor<T> link;

    public ChainingEquivalenceExtractor(EquivalenceExtractor<T> link) {
        this.link = link;
    }
    
    @Override
    public Maybe<ScoredCandidate<T>> extract(T target, List<ScoredCandidate<T>> equivalents, ResultDescription desc) {
        return extract(target, equivalents, link.extract(target, equivalents, desc), desc);
    }

    protected abstract Maybe<ScoredCandidate<T>> extract(T target, List<ScoredCandidate<T>> equivalents, Maybe<ScoredCandidate<T>> delegateExtraction, ResultDescription desc);
}
