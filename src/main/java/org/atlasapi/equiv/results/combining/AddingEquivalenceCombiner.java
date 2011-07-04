package org.atlasapi.equiv.results.combining;

import java.util.HashMap;
import java.util.Map;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class AddingEquivalenceCombiner<T extends Content> extends FoldingEquivalenceCombiner<T> {

    public static <T extends Content> AddingEquivalenceCombiner<T> create() {
        return new AddingEquivalenceCombiner<T>();
    }

    @Override
    public ScoredEquivalents<T> combine(ScoredEquivalents<T> combined, ScoredEquivalents<T> scoredEquivalents) {
        if(combined == null) {
            return scoredEquivalents;
        }
        
        Map<Publisher, Map<T, Score>> combinedMappedEquivalents = combined.equivalents();
        Map<Publisher, Map<T, Score>> scoredMappedEquivalents = scoredEquivalents.equivalents();
        
        Map<Publisher, Map<T, Score>> result = Maps.newHashMap();
        for (Publisher publisher : ImmutableSet.copyOf(Iterables.concat(combinedMappedEquivalents.keySet(), scoredMappedEquivalents.keySet()))) {
            result.put(publisher, combine(combinedMappedEquivalents.get(publisher), scoredMappedEquivalents.get(publisher)));
        }
        return DefaultScoredEquivalents.fromMappedEquivs(String.format("%s/%s", combined.source(), scoredEquivalents.source()), result);
    }
    
    public Map<T, Score> combine(Map<T, Score> left, Map<T, Score> right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        HashMap<T, Score> combined = Maps.newHashMap();
        for (T equiv : ImmutableSet.copyOf(Iterables.concat(left.keySet(), right.keySet()))) {
            combined.put(equiv, add(left.get(equiv),right.get(equiv)));
        }
        return combined;
    }

    private Score add(Score left, Score right) {
        return left == null ? right : right == null ? left : left.add(right);
    }

}
