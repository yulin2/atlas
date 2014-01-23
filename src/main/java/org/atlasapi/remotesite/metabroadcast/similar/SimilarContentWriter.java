package org.atlasapi.remotesite.metabroadcast.similar;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.SimilarContentRef;


public interface SimilarContentWriter {

    void write(Content sourceContent, Iterable<SimilarContentRef> similar);

}
