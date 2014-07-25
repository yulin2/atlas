package org.atlasapi.remotesite.btvod;

import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;

public interface BtVodDataProcessor<T> {

    boolean process(BtVodDataRow row);
    
    T getResult();
    
}
