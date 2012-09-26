package org.atlasapi.equiv.generators;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredCandidates;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EquivalenceGenerators<T> {
    
    public static <T> EquivalenceGenerators<T> from(Iterable<? extends EquivalenceGenerator<T>> generators) {
        return new EquivalenceGenerators<T>(generators);
    }

    private final List<? extends EquivalenceGenerator<T>> generators;
    
    public EquivalenceGenerators(Iterable<? extends EquivalenceGenerator<T>> generators) {
        this.generators = ImmutableList.copyOf(generators);
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
                throw new RuntimeException(String.format("Exception running %s for %s", generator, content), e);
            }
        }
        
        desc.finishStage();
        return generatedScores.build();
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(getClass())
                .add("generators", generators)
                .toString();
    }
}
