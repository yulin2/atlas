package org.atlasapi.remotesite.metabroadcast.similar;

import java.util.List;

import org.atlasapi.media.entity.Described;


public interface SimilarContentProvider {

    List<Described> similarTo(Described described);

}
