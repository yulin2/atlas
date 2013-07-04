package org.atlasapi.remotesite.redux;

import com.metabroadcast.common.scheduling.Reducible;

public interface ResultProcessor<I, O extends Reducible<O>> {

    O process(I input);
    
}
