package org.atlasapi.remotesite;

import com.metabroadcast.common.scheduling.UpdateProgress;


public interface DataProcessor<T> {

    boolean process(T item);
    
    UpdateProgress getResult();
}
