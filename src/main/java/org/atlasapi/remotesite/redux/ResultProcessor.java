package org.atlasapi.remotesite.redux;

import org.atlasapi.feeds.utils.Reducible;

public interface ResultProcessor<I, O extends Reducible<O>> {

    O process(I input);
    
}
