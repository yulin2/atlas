package org.atlasapi.remotesite.bt.channels.mpxclient;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;


public class Content {

    private List<String> assetTypes;
    private String sourceUrl;
    
    public Content() {
        
    }
    
    @VisibleForTesting
    public Content(Iterable<String> assetTypes, String sourceUrl) {
        this.assetTypes = ImmutableList.copyOf(assetTypes);
        this.sourceUrl = sourceUrl;
    }
    
    public List<String> getAssetTypes() {
        return assetTypes;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    
}
