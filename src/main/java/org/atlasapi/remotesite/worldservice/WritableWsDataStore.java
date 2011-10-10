package org.atlasapi.remotesite.worldservice;

import java.io.IOException;

public interface WritableWsDataStore extends WsDataStore {

    WsDataSet write(WsDataSet data) throws IOException;
    
}
