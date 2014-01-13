package org.atlasapi.remotesite.metabroadcast.similar;

import org.atlasapi.media.entity.ChildRef;


public interface SimilarContentWriter {

    void write(String sourceUri, Iterable<ChildRef> similar);

}
