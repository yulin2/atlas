package org.atlasapi.remotesite.youview;

import org.atlasapi.media.entity.Publisher;

import nu.xom.Element;

public interface YouViewXmlElementHandler {
    void handle(Publisher targetPublisher, Element element);
}
