package org.atlasapi.equiv.results;

import static com.google.common.collect.Ordering.natural;
import static org.atlasapi.equiv.results.scores.ScoredEquivalent.equivalentScore;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.combining.EquivalenceCombiner;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.base.Maybe;

public class DefaultEquivalenceResultBuilder<T extends Content> implements EquivalenceResultBuilder<T> {

    public static <T extends Content> EquivalenceResultBuilder<T> resultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> marker) {
        return new DefaultEquivalenceResultBuilder<T>(combiner, marker);
    }

    private final EquivalenceCombiner<T> combiner;
    private final EquivalenceExtractor<T> extractor;
    
    public DefaultEquivalenceResultBuilder(EquivalenceCombiner<T> combiner, EquivalenceExtractor<T> extractor) {
        this.combiner = combiner;
        this.extractor = extractor;
    }

    @Override
    public EquivalenceResult<T> resultFor(T target, List<ScoredEquivalents<T>> equivalents, ReadableDescription desc) {
        desc.startStage("Combining scores");
        ScoredEquivalents<T> combined = combine(equivalents, desc);
        
        desc.finishStage().startStage("Extracting strong equivalents");
        Map<Publisher, ScoredEquivalent<T>> extractedScores = extract(target, combined, desc);
        
        desc.finishStage();
        return new EquivalenceResult<T>(target, equivalents, combined, extractedScores, desc);
    }

    private Map<Publisher, ScoredEquivalent<T>> extract(T target, ScoredEquivalents<T> combined, ResultDescription desc) {
        Map<Publisher, Collection<ScoredEquivalent<T>>> publisherBins = publisherBin(combined.equivalents());
        
        Builder<Publisher, ScoredEquivalent<T>> builder = ImmutableMap.builder();
        
        for (Entry<Publisher, Collection<ScoredEquivalent<T>>> publisherBin : publisherBins.entrySet()) {
            desc.startStage(String.format("Publisher: %s", publisherBin.getKey()));
            
            Maybe<ScoredEquivalent<T>> extracted = extractor.extract(target, natural().reverse().immutableSortedCopy(publisherBin.getValue()), desc);
            
            if(extracted.hasValue()) {
                builder.put(publisherBin.getKey(), extracted.requireValue());
            }
            
            desc.finishStage();
        }
        
        return builder.build();
    }
    
    private Map<Publisher, Collection<ScoredEquivalent<T>>> publisherBin(Map<T, Score> equivalents) {
        Multimap<Publisher, ScoredEquivalent<T>> publisherBins = LinkedListMultimap.create();
        
        for (Entry<T, Score> equivalentScore : equivalents.entrySet()) {
            T key = equivalentScore.getKey();
            publisherBins.put(key.getPublisher(), equivalentScore(key, equivalentScore.getValue()));
        }
        
        return publisherBins.asMap();
    }

    private ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> equivalents, ResultDescription desc) {
        return !equivalents.isEmpty() ? combiner.combine(equivalents, desc) : DefaultScoredEquivalents.<T>fromSource("empty combination").build();
    }
}
