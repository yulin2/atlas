package org.atlasapi.remotesite.worldservice;

import org.joda.time.DateTime;

import com.metabroadcast.common.base.Maybe;


public interface WsDataStore {

    Maybe<WsDataSet> latestData();
    
    Maybe<WsDataSet> dataForDay(DateTime day);
    
}
