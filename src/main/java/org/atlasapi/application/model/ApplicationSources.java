package org.atlasapi.application.model;

import java.util.List;
import java.util.Map;

import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.entity.Publisher;
import org.elasticsearch.common.collect.ImmutableList;


public class ApplicationSources {
    private final boolean precedence;
    private final Map<Publisher, SourceStatus> reads;
    private final List<Publisher> writes;
    
    private ApplicationSources(Builder builder) {
        this.precedence = builder.precedence;
        this.reads = builder.reads;
        this.writes = ImmutableList.copyOf(builder.writes);
    }
    
    public boolean isPrecedenceEnabled() {
        return precedence;
    }
    
    public Map<Publisher, SourceStatus> getReads() {
        return reads;
    }
        
    public Iterable<Publisher> getWrites() {
        return writes;
    }
    
    public Builder copy() {
        return builder()
                .withPrecedence(this.isPrecedenceEnabled())
                .withReads(this.getReads())
                .withWrites(this.getWrites());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        public boolean precedence;
        private Map<Publisher, SourceStatus> reads;
        private Iterable<Publisher> writes;
        
        public Builder withPrecedence(boolean precedence) {
            this.precedence = precedence;
            return this;
        }
        
        public Builder withReads(Map<Publisher, SourceStatus> reads) {
            this.reads = reads;
            return this;
        }
        
        public Builder withWrites(Iterable<Publisher> writes) {
            this.writes = writes;
            return this;
        }
        
        public ApplicationSources build() {
            return new ApplicationSources(this);
        }
    }

}
