package org.atlasapi.equiv.results.scores;

import static org.atlasapi.equiv.results.scores.ScoredEquivalents.TO_SOURCE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class ScoredEquivalentsMerger {

    public <T extends Content> List<ScoredEquivalents<T>> merge(List<ScoredEquivalents<T>> lefts, List<ScoredEquivalents<T>> rights) {
        
        Map<String,ScoredEquivalents<T>> left = Maps.uniqueIndex(lefts, TO_SOURCE);
        ImmutableMap<String, ScoredEquivalents<T>> right = Maps.uniqueIndex(rights, TO_SOURCE);
        
        Map<String, ScoredEquivalents<T>> merged = Maps.newHashMap();
        
        for (String source : Iterables.concat(left.keySet(), right.keySet())) {
            if(!left.containsKey(source)) {
                merged.put(source, right.get(source));
            } else if(!right.containsKey(source)) {
                merged.put(source, left.get(source));
            } else {
                merged.put(source, merge(left.get(source), (right.get(source))));
            }
        }
        
        return ImmutableList.copyOf(merged.values());
        
    }

    private <T extends Content> ScoredEquivalents<T> merge(ScoredEquivalents<T> left, ScoredEquivalents<T> right) {
        HashMap<T, Score> rightMap = Maps.newHashMap(right.equivalents());
        rightMap.putAll(left.equivalents());
        return DefaultScoredEquivalents.fromMappedEquivs(left.source(), rightMap);
    }
   
}
