package org.atlasapi.equiv.results.combining;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class ItemScoreFilteringCombiner<T extends Content> implements EquivalenceCombiner<T> {

    private final EquivalenceCombiner<T> delegate;
    private final String source;

    public ItemScoreFilteringCombiner(EquivalenceCombiner<T> delegate, String source) {
        this.delegate = delegate;
        this.source = source;
    }
    
    @Override
    public ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents) {
        ScoredEquivalents<T> combined = delegate.combine(scoredEquivalents);
        
        ScoredEquivalents<T> itemScores = findItemScores(scoredEquivalents);
        
        if(itemScores == null) {
            return combined;
        }
        
        final Map<T, Score> itemScoreMap = itemScores.equivalents();
        
        Map<T, Score> transformedCombined = Maps.newHashMap(Maps.transformEntries(combined.equivalents(), new EntryTransformer<T, Score, Score>() {
            @Override
            public Score transformEntry(T equiv, Score combinedScore) {
                Score itemScore = itemScoreMap.get(equiv);
                
                if (itemScore == null || itemScore == Score.NULL_SCORE || !(itemScore.asDouble() > 0.0)) {
                    return Score.NULL_SCORE;
                }

                return itemScore;
            }
        }));
        
        return DefaultScoredEquivalents.fromMappedEquivs(combined.source(), transformedCombined);
    }
    
    private ScoredEquivalents<T> findItemScores(List<ScoredEquivalents<T>> scoredEquivalents) {
        for (ScoredEquivalents<T> sourceEquivs : scoredEquivalents) {
            if(sourceEquivs.source().equals(source)) {
                return sourceEquivs;
            }
        }
        return null;
    }

}
