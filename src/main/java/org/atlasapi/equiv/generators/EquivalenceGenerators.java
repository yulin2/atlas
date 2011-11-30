package org.atlasapi.equiv.generators;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;

import com.google.common.collect.Lists;

public class EquivalenceGenerators<T extends Content> {
    
    public static <T extends Content> EquivalenceGenerators<T> from(Iterable<ContentEquivalenceGenerator<T>> generators, AdapterLog log) {
        return new EquivalenceGenerators<T>(generators, log);
    }

    private final Iterable<ContentEquivalenceGenerator<T>> generators;
    private final AdapterLog log;
    
    public EquivalenceGenerators(Iterable<ContentEquivalenceGenerator<T>> generators, AdapterLog log) {
        this.generators = generators;
        this.log = log;
    }
    
    public List<ScoredEquivalents<T>> generate(T content, ResultDescription desc) {
        List<ScoredEquivalents<T>> generatedScores = Lists.newArrayList();
        
        desc.startStage("Generating equivalences");
        
        for (ContentEquivalenceGenerator<T> generator : generators) {
            try {
                generatedScores.add(generator.generate(content, desc));
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription(
                        "Exception running generator %s for %s %s", generator.getClass().getSimpleName(), content.getClass().getSimpleName(), content.getCanonicalUri()
                ));
                //TODO: propagate? It a generator fails we probably want to stop updating because it could change the result, splitting equivs.
            }
            
        }
        
        desc.finishStage();
        
        return generatedScores;
    }
    
}
