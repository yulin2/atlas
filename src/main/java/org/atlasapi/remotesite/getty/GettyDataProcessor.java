package org.atlasapi.remotesite.getty;

public interface GettyDataProcessor<T> {

    boolean process(String response);
    
    T getResult();
    
}
