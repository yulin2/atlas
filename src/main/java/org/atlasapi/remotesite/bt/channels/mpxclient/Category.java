package org.atlasapi.remotesite.bt.channels.mpxclient;

import com.google.common.annotations.VisibleForTesting;


public class Category {

    private String name;
    private String scheme;
    private String label;

    public Category() {
        
    }
    
    @VisibleForTesting
    public Category(String name, String scheme, String label) {
        this.name = name;
        this.scheme = scheme;
        this.label = label;
    }
    
    public String getName() {
        return name;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public String getLabel() {
        return label;
    }
    
}
