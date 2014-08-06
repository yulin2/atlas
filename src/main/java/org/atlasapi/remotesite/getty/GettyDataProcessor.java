package org.atlasapi.remotesite.getty;

public interface GettyDataProcessor<T> {

    boolean process(String token, String response);
    
    T getResult();
    
}
