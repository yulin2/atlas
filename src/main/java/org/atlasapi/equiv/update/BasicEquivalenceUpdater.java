package org.atlasapi.equiv.update;

import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;

import java.util.List;
import java.util.Set;

import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Deprecated
public class BasicEquivalenceUpdater<T extends Content> implements ContentEquivalenceUpdater<T> {

    private final Set<ContentEquivalenceGenerator<T>> calculators;
    private final EquivalenceResultBuilder<T> builder;
    private final AdapterLog log;

    public BasicEquivalenceUpdater(Set<ContentEquivalenceGenerator<T>> calculators, EquivalenceResultBuilder<T> builder, AdapterLog log) {
        this.calculators = calculators;
        this.builder = builder;
        this.log = log;
    }
    
    public EquivalenceResult<T> updateEquivalences(final T content) {
        
        Set<T> suggestions = Sets.newHashSet();
        List<ScoredEquivalents<T>> scores = Lists.newArrayList();
        ResultDescription desc = new DefaultDescription();
        
        for (ContentEquivalenceGenerator<T> calculator : calculators) {
            try {
                ScoredEquivalents<T> scoredEquivalents = calculator.generate(content, desc);
                suggestions.addAll(scoredEquivalents.equivalents().keySet());
                scores.add(scoredEquivalents);
            }catch (Exception e) {
                log.record(new AdapterLogEntry(WARN)
                    .withSource(getClass())
                    .withCause(e)
                    .withDescription(String.format("Exception in equivalence generator %s for %s", calculator.getClass().getSimpleName(), content.getCanonicalUri()))
                );
            }
        }
        
        return builder.resultFor(content, scores);
    }
    
}
