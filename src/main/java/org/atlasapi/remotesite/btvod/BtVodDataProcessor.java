package org.atlasapi.remotesite.btvod;

import nu.xom.Element;

public interface BtVodDataProcessor<T> {

    boolean process(Element element);
    
    T getResult();
}
