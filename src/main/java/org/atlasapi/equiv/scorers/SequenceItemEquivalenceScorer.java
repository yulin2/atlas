package org.atlasapi.equiv.scorers;

import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class SequenceItemEquivalenceScorer implements ContentEquivalenceScorer<Item> {

    @Override
    public ScoredEquivalents<Item> score(Item subject, Iterable<Item> suggestions) {
        ScoredEquivalentsBuilder<Item> equivalents = DefaultScoredEquivalents.fromSource("Sequence");
        
        for (Item suggestion : Iterables.filter(ImmutableSet.copyOf(suggestions), Item.class)) {
            equivalents.addEquivalent(suggestion, score(subject, suggestion));
        }
        
        return equivalents.build();
    }

    private Score score(Item subject, Item suggestion) {
        
        if (subject instanceof Episode && suggestion instanceof Episode) {

            Episode subEp = (Episode) subject;
            Episode sugEp = (Episode) suggestion;

            if (Objects.equal(subEp.getSeriesNumber(), sugEp.getSeriesNumber()) && subEp.getEpisodeNumber() != null && sugEp.getEpisodeNumber() != null
                    && Objects.equal(subEp.getEpisodeNumber(), sugEp.getEpisodeNumber())) {
                return Score.valueOf(1.0);
            }
        }
            
        
        return Score.NULL_SCORE;
    }

}
