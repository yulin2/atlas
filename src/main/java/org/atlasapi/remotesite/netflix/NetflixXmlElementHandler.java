package org.atlasapi.remotesite.netflix;

import nu.xom.Element;

public interface NetflixXmlElementHandler {
    void prepare();

    void handle(Element element);

    void finish();
}
