package org.atlasapi.equiv.results.combining;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class NullScoreAwareAveragingCombiner<T extends Content> implements EquivalenceCombiner<T> {

    @Override
    public ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents) {
        
        Map<Publisher, Map<T,Score>> tempResults = Maps.newHashMap();
        final Map<T, Integer> counts = Maps.newHashMap();
        List<String> source = Lists.newArrayListWithCapacity(tempResults.size());
        
        for (ScoredEquivalents<T> sourceEquivalents : scoredEquivalents) {
            source.add(sourceEquivalents.source());
            
            for (Entry<Publisher, Map<T, Score>> equivalentsMap : sourceEquivalents.equivalents().entrySet()) {
                
                Map<T, Score> curRes = tempResults.get(equivalentsMap.getKey());
                
                if(curRes == null) {
                    for (T equiv : Maps.filterValues(equivalentsMap.getValue(), Score.IS_REAL_SCORE).keySet()) {
                        counts.put(equiv, 1);
                    }
                    tempResults.put(equivalentsMap.getKey(), Maps.newHashMap(equivalentsMap.getValue()));
                } else {
                    for (Entry<T, Score> equivScore : equivalentsMap.getValue().entrySet()) {
                        
                        if(equivScore.getValue().isRealScore()) {
                            Integer curCount = counts.get(equivScore.getKey());
                            counts.put(equivScore.getKey(), curCount != null ? curCount + 1 : 1);
                        }
                        
                        Score curCombinedScore = curRes.get(equivScore.getKey());
                        curRes.put(equivScore.getKey(), curCombinedScore != null ? curCombinedScore.add(equivScore.getValue()) : equivScore.getValue());
                    }
                }
                
            }
            
        }
        
        Map<Publisher, Map<T, Score>> scaledScores = Maps.transformValues(tempResults, new Function<Map<T, Score>, Map<T, Score>>() {
            @Override
            public Map<T, Score> apply(Map<T, Score> input) {
                return Maps.transformEntries(input, new EntryTransformer<T, Score, Score>() {

                    @Override
                    public Score transformEntry(T key, Score value) {
                        if(value.isRealScore()) {
                            Integer count = counts.get(key);
                            return Score.valueOf(value.asDouble() / (count != null ? count : 1));
                        } else {
                            return value;
                        }
                    }
                });
            }
        });
        
        return DefaultScoredEquivalents.fromMappedEquivs(Joiner.on("/").join(source), scaledScores);
    }

}
