package org.atlasapi.remotesite.youview;

import nu.xom.Element;

import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;


public interface YouViewElementProcessor {
    
    ItemRefAndBroadcast process(Element element);
}
