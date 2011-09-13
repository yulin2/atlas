package org.atlasapi.remotesite.worldservice;

import org.joda.time.DateTime;

import com.metabroadcast.common.base.Maybe;


public interface WsDataStore {

    Maybe<WsData> latestData();
    
    Maybe<WsData> dataForDay(DateTime day);
    
}
