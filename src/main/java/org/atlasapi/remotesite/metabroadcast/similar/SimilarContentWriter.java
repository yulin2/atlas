package org.atlasapi.remotesite.metabroadcast.similar;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;


public interface SimilarContentWriter {

    void write(Content sourceContent, Iterable<ChildRef> similar);

}
