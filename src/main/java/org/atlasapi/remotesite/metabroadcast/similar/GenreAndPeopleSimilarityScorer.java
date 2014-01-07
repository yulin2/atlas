package org.atlasapi.remotesite.metabroadcast.similar;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Item;

import com.google.common.collect.Sets;

public class GenreAndPeopleSimilarityScorer implements SimilarityScorer {

    @Override
    public int score(Described d1, Described d2) {
        return Sets.intersection(d1.getGenres(), d2.getGenres()).size()
                + peopleInCommon(d1, d2);
    }
    
    private int peopleInCommon(Described d1, Described d2) {
        if (! ( d1 instanceof Item && d2 instanceof Item)) {
            return 0;
        }
        
        Item i1 = (Item) d1;
        Item i2 = (Item) d2;
        
        return Sets.intersection(Sets.newHashSet(i1.getPeople()), 
                                 Sets.newHashSet(i2.getPeople())).size();
    }

}
