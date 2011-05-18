package org.atlasapi.equiv.results.combining;

import java.util.HashMap;
import java.util.Map;

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
        
        Map<Publisher, Map<T, Double>> combinedMappedEquivalents = combined.equivalents();
        Map<Publisher, Map<T, Double>> scoredMappedEquivalents = scoredEquivalents.equivalents();
        
        Map<Publisher, Map<T, Double>> result = Maps.newHashMap();
        for (Publisher publisher : ImmutableSet.copyOf(Iterables.concat(combinedMappedEquivalents.keySet(), scoredMappedEquivalents.keySet()))) {
            result.put(publisher, combine(combinedMappedEquivalents.get(publisher), scoredMappedEquivalents.get(publisher)));
        }
        return ScoredEquivalents.fromMappedEquivs(String.format("%s/%s", combined.source(), scoredEquivalents.source()), result);
    }
    
    public Map<T, Double> combine(Map<T, Double> left, Map<T, Double> right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        HashMap<T, Double> combined = Maps.newHashMap();
        for (T equiv : ImmutableSet.copyOf(Iterables.concat(left.keySet(), right.keySet()))) {
            combined.put(equiv, add(left.get(equiv),right.get(equiv)));
        }
        return combined;
    }

    private Double add(Double left, Double right) {
        return left != null ? (right != null ? left + right : left) : right;
    }

}
