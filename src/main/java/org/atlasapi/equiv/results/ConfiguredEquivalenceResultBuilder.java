package org.atlasapi.equiv.results;

import static org.atlasapi.equiv.results.DefaultEquivalenceResultBuilder.resultBuilder;
import static org.atlasapi.equiv.results.extractors.PercentThresholdEquivalenceExtractor.moreThanPercent;
import static org.atlasapi.equiv.update.ContainerEquivalenceUpdater.ITEM_UPDATER_NAME;

import java.util.List;

import org.atlasapi.equiv.results.combining.ScoreCombiner;
import org.atlasapi.equiv.results.combining.ItemScoreFilteringCombiner;
import org.atlasapi.equiv.results.combining.NullScoreAwareAveragingCombiner;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.MinimumScoreEquivalenceExtractor;
import org.atlasapi.equiv.results.extractors.PublisherFilteringExtractor;
import org.atlasapi.equiv.results.extractors.SpecializationMatchingEquivalenceExtractor;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Content;

public class ConfiguredEquivalenceResultBuilder<T extends Content> implements EquivalenceResultBuilder<T> {

    private EquivalenceResultBuilder<T> builder;

    public ConfiguredEquivalenceResultBuilder() {
        
        ScoreCombiner<T> combiner = new ItemScoreFilteringCombiner<T>(NullScoreAwareAveragingCombiner.<T>get(), ITEM_UPDATER_NAME);
        EquivalenceExtractor<T> extractor = moreThanPercent(90);
        extractor = new MinimumScoreEquivalenceExtractor<T>(extractor, 0.2);
        extractor = new SpecializationMatchingEquivalenceExtractor<T>(extractor);
        extractor = new PublisherFilteringExtractor<T>(extractor);

        this.builder = resultBuilder(combiner, extractor);
    }

    @Override
    public EquivalenceResult<T> resultFor(T target, List<ScoredCandidates<T>> equivalents, ReadableDescription desc) {
        return builder.resultFor(target, equivalents, desc);
    }

}
