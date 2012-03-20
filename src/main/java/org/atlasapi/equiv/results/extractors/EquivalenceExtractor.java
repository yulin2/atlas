package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.media.content.Content;

import com.metabroadcast.common.base.Maybe;

public interface EquivalenceExtractor<T extends Content> {

    /**
     * Extracts a single 'strongly' equivalent piece of content from an ordered list of weighted suggestions.
     * @param target the subject content
     * @param equivalents - list of ordered equivalence suggestions for a single publisher.
     * @param desc TODO
     * @return maybe a strong equivalent or nothing if none of the suggestions  
     */
    Maybe<ScoredEquivalent<T>> extract(T target, List<ScoredEquivalent<T>> equivalents, ResultDescription desc);
    
}
