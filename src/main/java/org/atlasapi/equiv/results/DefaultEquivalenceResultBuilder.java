package org.atlasapi.equiv.results;

import static com.google.common.collect.Ordering.natural;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.combining.ScoreCombiner;
import org.atlasapi.equiv.results.description.ReadableDescription;
import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.extractors.EquivalenceExtractor;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.base.Maybe;

public class DefaultEquivalenceResultBuilder<T extends Content> implements EquivalenceResultBuilder<T> {

    public static <T extends Content> EquivalenceResultBuilder<T> resultBuilder(ScoreCombiner<T> combiner, EquivalenceExtractor<T> marker) {
        return new DefaultEquivalenceResultBuilder<T>(combiner, marker);
    }

    private final ScoreCombiner<T> combiner;
    private final EquivalenceExtractor<T> extractor;
    
    public DefaultEquivalenceResultBuilder(ScoreCombiner<T> combiner, EquivalenceExtractor<T> extractor) {
        this.combiner = combiner;
        this.extractor = extractor;
    }

    @Override
    public EquivalenceResult<T> resultFor(T target, List<ScoredCandidates<T>> equivalents, ReadableDescription desc) {
        desc.startStage("Combining scores");
        ScoredCandidates<T> combined = combine(equivalents, desc);
        
        desc.finishStage().startStage("Extracting strong equivalents");
        Map<Publisher, ScoredCandidate<T>> extractedScores = extract(target, combined, desc);
        
        desc.finishStage();
        return new EquivalenceResult<T>(target, equivalents, combined, extractedScores, desc);
    }

    private Map<Publisher, ScoredCandidate<T>> extract(T target, ScoredCandidates<T> combined, ResultDescription desc) {
        Map<Publisher, Collection<ScoredCandidate<T>>> publisherBins = publisherBin(combined.candidates());
        
        Builder<Publisher, ScoredCandidate<T>> builder = ImmutableMap.builder();
        
        for (Entry<Publisher, Collection<ScoredCandidate<T>>> publisherBin : publisherBins.entrySet()) {
            desc.startStage(String.format("Publisher: %s", publisherBin.getKey()));
            
            Maybe<ScoredCandidate<T>> extracted = extractor.extract(target, natural().reverse().immutableSortedCopy(publisherBin.getValue()), desc);
            
            if(extracted.hasValue()) {
                builder.put(publisherBin.getKey(), extracted.requireValue());
            }
            
            desc.finishStage();
        }
        
        return builder.build();
    }
    
    private Map<Publisher, Collection<ScoredCandidate<T>>> publisherBin(Map<T, Score> equivalents) {
        Multimap<Publisher, ScoredCandidate<T>> publisherBins = LinkedListMultimap.create();
        
        for (Entry<T, Score> equivalentScore : equivalents.entrySet()) {
            T key = equivalentScore.getKey();
            publisherBins.put(key.getPublisher(), ScoredCandidate.valueOf(key, equivalentScore.getValue()));
        }
        
        return publisherBins.asMap();
    }

    private ScoredCandidates<T> combine(List<ScoredCandidates<T>> equivalents, ResultDescription desc) {
        return !equivalents.isEmpty() ? combiner.combine(equivalents, desc) : DefaultScoredEquivalents.<T>fromSource("empty combination").build();
    }
}
