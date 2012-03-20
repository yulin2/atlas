package org.atlasapi.equiv.results.combining;

import java.util.List;
import java.util.Map;

import org.atlasapi.equiv.results.description.ResultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoreThreshold;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Content;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class ItemScoreFilteringCombiner<T extends Content> implements EquivalenceCombiner<T> {

    private final EquivalenceCombiner<T> delegate;
    private final String source;
    private final ScoreThreshold threshold;

    public ItemScoreFilteringCombiner(EquivalenceCombiner<T> delegate, String source, ScoreThreshold threshold) {
        this.delegate = delegate;
        this.source = source;
        this.threshold = threshold;
    }
    
    public ItemScoreFilteringCombiner(EquivalenceCombiner<T> delegate, String source) {
        this(delegate, source, ScoreThreshold.POSITIVE);
    }
    
    @Override
    public ScoredEquivalents<T> combine(List<ScoredEquivalents<T>> scoredEquivalents, final ResultDescription desc) {
        ScoredEquivalents<T> combined = delegate.combine(scoredEquivalents, desc);
        
        desc.startStage("Filtering null " +  source + " scores");
        
        ScoredEquivalents<T> itemScores = findItemScores(scoredEquivalents);
        
        if(itemScores == null) {
            desc.appendText("No %s scores found", source).finishStage();
            return combined;
        }
        
        final Map<T, Score> itemScoreMap = itemScores.equivalents();
        
        Map<T, Score> transformedCombined = ImmutableMap.copyOf(Maps.transformEntries(combined.equivalents(), new EntryTransformer<T, Score, Score>() {
            @Override
            public Score transformEntry(T equiv, Score combinedScore) {
                Score itemScore = itemScoreMap.get(equiv);

                if (threshold.apply(itemScore)) {
                    return combinedScore;
                }
                
                desc.appendText("%s score set to null, %s score %s", equiv.getCanonicalUri(), source, itemScore);
                return Score.NULL_SCORE;
            }
        }));
        desc.finishStage();
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
