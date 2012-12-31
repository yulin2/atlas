package org.atlasapi.equiv.results.combining;

import java.util.Map;

import org.atlasapi.equiv.results.scores.DefaultScoredCandidates;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.content.Content;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class AddingEquivalenceCombiner<T extends Content> extends FoldingEquivalenceCombiner<T> {

    public static <T extends Content> AddingEquivalenceCombiner<T> create() {
        return new AddingEquivalenceCombiner<T>();
    }

    @Override
    public ScoredCandidates<T> combine(ScoredCandidates<T> combined, ScoredCandidates<T> scoredEquivalents) {
        if(combined == null) {
            return scoredEquivalents;
        }
        
        Map<T, Score> combinedMappedEquivalents = combined.candidates();
        Map<T, Score> scoredMappedEquivalents = scoredEquivalents.candidates();
        
        Map<T, Score> result = Maps.newHashMap();
        for (T content : ImmutableSet.copyOf(Iterables.concat(combinedMappedEquivalents.keySet(), scoredMappedEquivalents.keySet()))) {
            result.put(content, add(combinedMappedEquivalents.get(content), scoredMappedEquivalents.get(content)));
        }
        return DefaultScoredCandidates.fromMappedEquivs(String.format("%s/%s", combined.source(), scoredEquivalents.source()), result);
    }
    
    private Score add(Score left, Score right) {
        return left == null ? right : right == null ? left : left.add(right);
    }

}
