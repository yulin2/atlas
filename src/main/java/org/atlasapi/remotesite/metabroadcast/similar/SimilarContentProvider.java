package org.atlasapi.remotesite.metabroadcast.similar;

import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Described;


public interface SimilarContentProvider {

    void initialise();
    List<ChildRef> similarTo(Described described);

}
