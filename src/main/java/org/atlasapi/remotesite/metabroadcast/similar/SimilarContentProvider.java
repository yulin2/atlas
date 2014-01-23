package org.atlasapi.remotesite.metabroadcast.similar;

import java.util.List;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.SimilarContentRef;


public interface SimilarContentProvider {

    void initialise();
    List<SimilarContentRef> similarTo(Described described);

}
