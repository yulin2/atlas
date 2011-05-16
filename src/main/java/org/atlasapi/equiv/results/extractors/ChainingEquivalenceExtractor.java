package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

import com.metabroadcast.common.base.Maybe;

public abstract class ChainingEquivalenceExtractor<T extends Content> implements EquivalenceExtractor<T> {

    private final EquivalenceExtractor<T> link;

    public ChainingEquivalenceExtractor(EquivalenceExtractor<T> link) {
        this.link = link;
    }
    
    @Override
    public Maybe<ScoredEquivalent<T>> extract(List<ScoredEquivalent<T>> equivalents) {
        return extract(equivalents, link.extract(equivalents));
    }

    protected abstract Maybe<ScoredEquivalent<T>> extract(List<ScoredEquivalent<T>> equivalents, Maybe<ScoredEquivalent<T>> delegateExtraction);
}
