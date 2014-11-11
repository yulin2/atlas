package org.atlasapi.equiv.results.filters;

import java.util.Set;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.media.entity.Identified;

import com.google.common.collect.ImmutableSet;


public class ExclusionListFilter<T extends Identified> extends AbstractEquivalenceFilter<T> {

    private final Set<String> excludedUris;

    public ExclusionListFilter(Iterable<String> excludedUris) {
        this.excludedUris = ImmutableSet.copyOf(excludedUris);
    }
    
    @Override
    protected boolean doFilter(ScoredCandidate<T> candidate, T subject, ResultDescription desc) {
        boolean result = !excludedUris.contains(candidate.candidate().getCanonicalUri());
        if (!result) {
            desc.appendText("%s removed as contained in exclusion list", 
                candidate.candidate().getCanonicalUri());
        }
        return result;
    }

}
