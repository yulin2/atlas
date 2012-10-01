package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;

public interface LoveFilmDataProcessor<T> {

    boolean process(LoveFilmDataRow row);
    
    T getResult();
    
}
