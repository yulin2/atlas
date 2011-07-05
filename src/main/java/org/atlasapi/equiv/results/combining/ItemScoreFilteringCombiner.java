package org.atlasapi.equiv.results.combining;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.generators.ItemBasedContainerEquivalenceGenerator;
import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.Score;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class ItemScoreFilteringCombiner<T extends Content> implements EquivalenceCombiner<T> {

    private final EquivalenceCombiner<T> delegate;

    public ItemScoreFilteringCombiner(EquivalenceCombiner<T> delegate) {
        this.delegate = delegate;
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
            if(sourceEquivs.source().equals(ItemBasedContainerEquivalenceGenerator.NAME)) {
                return sourceEquivs;
            }
        }
        return null;
    }

}
