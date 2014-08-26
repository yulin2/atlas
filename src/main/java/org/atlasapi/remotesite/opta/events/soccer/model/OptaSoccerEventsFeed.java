package org.atlasapi.remotesite.opta.events.soccer.model;

import com.google.gson.annotations.SerializedName;


public class OptaSoccerEventsFeed {

    @SerializedName("SoccerFeed")
    private OptaSoccerFeed feed;
    
    public OptaSoccerEventsFeed() { }
    
    public OptaSoccerFeed feed() {
        return feed;
    }
    
    public static class OptaSoccerFeed {
        
        @SerializedName("SoccerDocument")
        private OptaSoccerDocument document;
        
        public OptaSoccerFeed() { }
        
        public OptaSoccerDocument document() {
            return document;
        }
    }
}
