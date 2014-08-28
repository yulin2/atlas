package org.atlasapi.remotesite.opta.events.soccer.model;

import com.google.gson.annotations.SerializedName;


public class SoccerStats {

    @SerializedName("@value")
    private String value;
    @SerializedName("@attributes")
    private StatsAttributes attributes;
    
    public SoccerStats() { }
    
    public String value() {
        return value;
    }
    
    public StatsAttributes attributes() {
        return attributes;
    }
    
    public static class StatsAttributes {
        
        @SerializedName("Type")
        private String type;
        
        public StatsAttributes() { }
        
        public String type() {
            return type;
        }
    }
}
