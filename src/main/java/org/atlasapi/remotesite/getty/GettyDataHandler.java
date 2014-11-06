package org.atlasapi.remotesite.getty;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;

interface GettyDataHandler {

    Identified handle(VideoResponse video);
    void write(Content content);

}
