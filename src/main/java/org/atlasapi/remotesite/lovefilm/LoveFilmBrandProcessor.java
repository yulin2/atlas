package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;


public interface LoveFilmBrandProcessor {

    void prepare();

    void handle(LoveFilmDataRow row);

    void finish();
    
    BrandType getBrandType(String uri);
}