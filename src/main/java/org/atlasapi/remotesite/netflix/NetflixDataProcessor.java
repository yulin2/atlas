package org.atlasapi.remotesite.netflix;

import nu.xom.Element;

public interface NetflixDataProcessor<T> {
    
    boolean process(Element element);
    
    T getResult();
}
