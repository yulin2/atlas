package org.uriplay.equiv;

import org.uriplay.media.entity.Content;

public interface EquivUpdater<CONTENT extends Content> {
    void update(CONTENT content);
}