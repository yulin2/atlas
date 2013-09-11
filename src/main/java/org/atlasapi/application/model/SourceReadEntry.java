package org.atlasapi.application.model;

import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.entity.Publisher;

public class SourceReadEntry {
    private Publisher publisher;
    private SourceStatus sourceStatus;
    
    public SourceReadEntry(Publisher publisher, SourceStatus sourceStatus) {
        this.publisher = publisher;
        this.sourceStatus = sourceStatus;
    }
    
    public Publisher getPublisher() {
        return publisher;
    }
    
    public SourceStatus getSourceStatus() {
        return sourceStatus;
    }   
}
