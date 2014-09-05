package org.atlasapi.remotesite.bt.events.feedModel;

import com.google.gson.annotations.SerializedName;


public class BtEventsFeed {

    @SerializedName("responseHeader")
    private BtResponseHeader header;
    private BtResponse response;
    
    public BtEventsFeed() { }
    
    public BtResponseHeader header() {
        return header;
    }
    
    public BtResponse response() {
        return response;
    }
}
