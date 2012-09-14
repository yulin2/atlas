package org.atlasapi.equiv.generators;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EquivalenceGenerators<T> {
    
    public static <T> EquivalenceGenerators<T> from(Iterable<EquivalenceGenerator<T>> generators) {
        return new EquivalenceGenerators<T>(generators);
    }

    private final Iterable<EquivalenceGenerator<T>> generators;
    
    public EquivalenceGenerators(Iterable<EquivalenceGenerator<T>> generators) {
        this.generators = generators;
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
                throw new RuntimeException(String.format("Exception running {} for {}", generator, content), e);
            }
        }
        
        desc.finishStage();
        return generatedScores.build();
    }
    
}
