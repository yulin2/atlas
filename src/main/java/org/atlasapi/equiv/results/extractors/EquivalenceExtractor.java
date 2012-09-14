package org.atlasapi.equiv.results.extractors;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;

import com.metabroadcast.common.base.Maybe;

public interface EquivalenceExtractor<T> {

    /**
     * Extracts a single 'strongly' equivalent piece of content from an ordered list of weighted suggestions.
     * @param target the subject content
     * @param equivalents - list of ordered equivalence suggestions for a single publisher.
     * @param desc TODO
     * @return maybe a strong equivalent or nothing if none of the suggestions  
     */
    Maybe<ScoredCandidate<T>> extract(T target, List<ScoredCandidate<T>> equivalents, ResultDescription desc);
    
}
