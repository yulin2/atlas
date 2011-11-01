package org.atlasapi.remotesite.worldservice;

import org.joda.time.DateTime;


public interface WsDataSet {
    
    DateTime getVersion();
    
    WsDataSource getDataForFile(WsDataFile file);
    
}
