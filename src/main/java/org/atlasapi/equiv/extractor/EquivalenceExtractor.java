package org.atlasapi.equiv.extractor;

import java.util.List;

import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.media.entity.Content;

public interface EquivalenceExtractor<T extends Content> {

    List<ScoredEquivalent<T>> extractFrom(List<ScoredEquivalent<T>> equivalents);
    
}
