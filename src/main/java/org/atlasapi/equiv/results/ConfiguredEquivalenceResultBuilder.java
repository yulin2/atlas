package org.atlasapi.equiv.results;

import static org.atlasapi.equiv.results.DefaultEquivalenceResultBuilder.resultBuilder;
import static org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor.extractorMoreThanPercent;
import static org.atlasapi.equiv.update.ContainerEquivalenceUpdater.ITEM_UPDATER_NAME;

import java.util.List;

import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
import org.atlasapi.equiv.results.combining.ItemScoreFilteringCombiner;
import org.atlasapi.equiv.results.combining.NullScoreAwareAveragingCombiner;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.MinimumScoreEquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.PublisherFilteringExtractor;
import org.atlasapi.equiv.results.extractors.SpecializationMatchingEquivalenceExtractor;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Content;

public class ConfiguredEquivalenceResultBuilder<T extends Content> implements EquivalenceResultBuilder<T> {

    private EquivalenceResultBuilder<T> builder;

    public ConfiguredEquivalenceResultBuilder() {
        
        EquivalenceCombiner<T> combiner = new ItemScoreFilteringCombiner<T>(new NullScoreAwareAveragingCombiner<T>(), ITEM_UPDATER_NAME);
        EquivalenceExtractor<T> extractor = extractorMoreThanPercent(90);
        extractor = new MinimumScoreEquivalenceExtractor<T>(extractor, 0.2);
        extractor = new SpecializationMatchingEquivalenceExtractor<T>(extractor);
        extractor = new PublisherFilteringExtractor<T>(extractor);

        this.builder = resultBuilder(combiner, extractor);
    }

    @Override
    public EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents, ReadableDescription desc) {
        return builder.resultFor(target, equivalents, desc);
    }

}
