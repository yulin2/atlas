package org.atlasapi.remotesite.btvod;

import nu.xom.Element;

public interface BtVodXmlElementHandler {
    void prepare();

    void handle(Element element);

    void finish();
}
