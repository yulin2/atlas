package org.atlasapi.equiv.generators;

import java.util.Set;

import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.DefaultScoredEquivalents.ScoredEquivalentsBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

public class SequenceItemEquivalenceScorer implements ContentEquivalenceGenerator<Item> {

    @Override
    public ScoredEquivalents<Item> generateEquivalences(Item subject, Set<Item> suggestions) {
        ScoredEquivalentsBuilder<Item> equivalents = DefaultScoredEquivalents.fromSource("Sequence");
        
        for (Item suggestion : Iterables.filter(suggestions, Item.class)) {
            equivalents.addEquivalent(suggestion, score(subject, suggestion));
        }
        
        return equivalents.build();
    }

    private double score(Item subject, Item suggestion) {
        
        if(!(subject instanceof Episode && suggestion instanceof Episode)) {
            return 0;
        }
        
        Episode subEp = (Episode) subject;
        Episode sugEp = (Episode) suggestion;
        
        if( Objects.equal(subEp.getSeriesNumber(), sugEp.getSeriesNumber()) &&
            subEp.getEpisodeNumber() != null && sugEp.getEpisodeNumber() != null && Objects.equal(subEp.getEpisodeNumber(), sugEp.getEpisodeNumber())) {
                return 1;
        }
            
        
        return 0;
    }

}
