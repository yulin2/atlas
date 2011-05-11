package org.atlasapi.equiv.extractor;

import java.util.Map;

import org.atlasapi.media.entity.Content;

public interface EquivalenceCombiner<T extends Content> {

    Map<T,Double> combine(Map<T,Double> left, Map<T,Double> right);
    
}
