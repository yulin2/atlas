package org.atlasapi.remotesite.worldservice;

import org.atlasapi.remotesite.worldservice.model.WsAudioItem;
import org.atlasapi.remotesite.worldservice.model.WsProgramme;

public interface WsProgrammeHandler {

    void handle(WsProgramme programme, Iterable<WsAudioItem> collection);
    
}
