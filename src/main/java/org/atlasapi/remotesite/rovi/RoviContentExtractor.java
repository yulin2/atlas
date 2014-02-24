package org.atlasapi.remotesite.rovi;

import org.atlasapi.media.entity.Content;


public interface RoviContentExtractor<SOURCE, CONTENT extends Content> {

    CONTENT extract(SOURCE source) throws IndexAccessException;
    
}
