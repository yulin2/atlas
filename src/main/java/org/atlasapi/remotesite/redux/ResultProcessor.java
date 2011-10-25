package org.atlasapi.remotesite.redux;

public interface ResultProcessor<I,O> {

    O process(I input);
    
}
