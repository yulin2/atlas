package org.atlasapi.equiv;

import org.atlasapi.media.entity.Content;

public interface EquivUpdater<CONTENT extends Content> {
	
    void update(CONTENT content);

}