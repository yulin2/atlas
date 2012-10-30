package org.atlasapi.input;

import org.atlasapi.media.entity.simple.Identified;

public interface ModelTransformer<F extends Identified, T extends org.atlasapi.media.entity.Identified> {

    T transform(F simple);
    
}
