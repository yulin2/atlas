package org.atlasapi.remotesite.metabroadcast.similar;

import java.util.Set;

import org.atlasapi.media.entity.Described;

public interface TraitHashCalculator {

    Set<Integer> traitHashesFor(Described d);
    
}