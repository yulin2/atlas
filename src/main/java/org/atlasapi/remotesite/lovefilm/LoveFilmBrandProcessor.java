package org.atlasapi.remotesite.lovefilm;


public interface LoveFilmBrandProcessor {

    void process(LoveFilmData data);
    
    BrandType getBrandType(String uri);
}