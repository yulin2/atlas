package org.atlasapi.remotesite.pa.features;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Publisher;


public class ContentGroupDetails {
    
    private final Publisher publisher;
    private final String uriBase;
    
    public ContentGroupDetails(Publisher publisher, String uriBase) {
        this.publisher = checkNotNull(publisher);
        this.uriBase = checkNotNull(uriBase);
    }
    
    public Publisher publisher() {
        return publisher;
    }
    
    public String uriBase() {
        return uriBase;
    }
}
