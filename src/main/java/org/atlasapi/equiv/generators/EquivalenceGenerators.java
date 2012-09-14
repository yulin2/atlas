package org.atlasapi.equiv.generators;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EquivalenceGenerators<T> {
    
    public static <T> EquivalenceGenerators<T> from(Iterable<EquivalenceGenerator<T>> generators, AdapterLog log) {
        return new EquivalenceGenerators<T>(generators, log);
    }

    private final Iterable<EquivalenceGenerator<T>> generators;
    private final AdapterLog log;
    
    public EquivalenceGenerators(Iterable<EquivalenceGenerator<T>> generators, AdapterLog log) {
        this.generators = generators;
        this.log = log;
    }
    
    public List<ScoredCandidates<T>> generate(T content, ResultDescription desc) {
        desc.startStage("Generating equivalences");
        Builder<ScoredCandidates<T>> generatedScores = ImmutableList.builder();
        
        for (EquivalenceGenerator<T> generator : generators) {
            try {
                desc.startStage(generator.toString());
                generatedScores.add(generator.generate(content, desc));
                desc.finishStage();
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Exception running %s for %s", generator, content));
                /* Propagate to make sure the equivalence update for this content fails - if a generator fails
                 * intermittently there's a risk of equivalence flip-flop.
                 */
                throw Throwables.propagate(e);
            }
        }
        
        desc.finishStage();
        return generatedScores.build();
    }
    
}
