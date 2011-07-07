package org.atlasapi.equiv.generators;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;

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
    
    public List<ScoredEquivalents<T>> generate(T content) {
        
        List<ScoredEquivalents<T>> generatedScores = Lists.newArrayList();
        
        for (ContentEquivalenceGenerator<T> generator : generators) {
            try {
                generatedScores.add(generator.generate(content));
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription(
                        "Exception running generator %s for %s %s", generator.getClass().getSimpleName(), content.getClass().getSimpleName(), content.getCanonicalUri()
                ));
            }
            
        }
        return generatedScores;
    }
    
}
