package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.content.Content;

import com.metabroadcast.common.base.Maybe;

public abstract class ChainingEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    private final EquivalenceExtractor<T> link;

    public ChainingEquivalenceExtractor(EquivalenceExtractor<T> link) {
        this.link = link;
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents, ResultDescription desc) {
        return extract(target, equivalents, link.extract(target, equivalents, desc), desc);
    }

    protected abstract Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents, Maybe<ScoredEquivalent<T>> delegateExtraction, ResultDescription desc);
}
