package org.atlasapi.equiv.results.combining;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class NullScoreAwareAveragingCombiner<T extends Content> implements EquivalenceCombiner<T> {

    @Override
    public ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents) {
        
        Map<T,Score> tempResults = Maps.newHashMap();
        final Map<T, Integer> counts = Maps.newHashMap();
        List<String> source = Lists.newArrayListWithCapacity(tempResults.size());
        
        for (ScoredEquivalents<T> sourceEquivalents : scoredEquivalents) {
            source.add(sourceEquivalents.source());
            
            for (Entry<T, Score> equivScore : sourceEquivalents.equivalents().entrySet()) {
                
                T equiv = equivScore.getKey();
                Score score = equivScore.getValue();
                
                Score curRes = tempResults.get(equiv);
                
                if(curRes == null) {
                    if(score.isRealScore()) {
                        counts.put(equiv, 1);
                    }
                    tempResults.put(equiv, score);
                } else {
                    if(score.isRealScore()) {
                        Integer curCount = counts.get(equivScore.getKey());
                        counts.put(equivScore.getKey(), curCount != null ? curCount + 1 : 1);
                    }
                    tempResults.put(equiv, curRes != null ? curRes.add(score) : score);
                }
            }
                
        }
        
        Map<T, Score> scaledScores = Maps.transformEntries(tempResults, new EntryTransformer<T, Score, Score>() {
            @Override
            public Score transformEntry(T key, Score value) {
                if (value.isRealScore()) {
                    Integer count = counts.get(key);
                    return Score.valueOf(value.asDouble() / (count != null ? count : 1));
                } else {
                    return value;
                }
            }
        });
        
        return DefaultScoredEquivalents.fromMappedEquivs(Joiner.on("/").join(source), scaledScores);
    }

}
