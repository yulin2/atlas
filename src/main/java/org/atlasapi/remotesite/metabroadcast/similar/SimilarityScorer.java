package org.atlasapi.remotesite.metabroadcast.similar;

import org.atlasapi.media.entity.Described;


public interface SimilarityScorer {

    int score(Described d1, Described d2);
    
}
