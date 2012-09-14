package org.atlasapi.equiv.generators;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;

public interface EquivalenceGenerator<T> {

    ScoredCandidates<T> generate(T subject, ResultDescription desc);
    
}
