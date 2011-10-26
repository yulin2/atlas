package org.atlasapi.remotesite.redux;

public interface ResultProcessor<I, O extends Reducible<O>> {

    O process(I input);
    
}
