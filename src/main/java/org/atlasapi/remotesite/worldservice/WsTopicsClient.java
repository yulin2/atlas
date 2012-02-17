package org.atlasapi.remotesite.worldservice;

import java.util.Map;

import org.atlasapi.remotesite.worldservice.model.WsTopics;

import com.metabroadcast.common.base.Maybe;

public interface WsTopicsClient {

    Maybe<Map<String, WsTopics>> getLatestTopics();
    
}
