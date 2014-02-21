package org.atlasapi.remotesite.rovi.deltas;

import org.atlasapi.media.entity.Content;


public interface ContentPopulator<CONTENT extends Content> {

    void populateContent(CONTENT content);
    
}
