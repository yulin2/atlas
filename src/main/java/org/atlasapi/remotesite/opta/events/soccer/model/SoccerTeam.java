package org.atlasapi.remotesite.opta.events.soccer.model;

import org.atlasapi.remotesite.opta.events.model.OptaTeam;

import com.google.gson.annotations.SerializedName;


public class SoccerTeam implements OptaTeam {
    
    @SerializedName("Name")
    private String name;
    @SerializedName("@attributes")
    private Attributes attributes;
    
    public SoccerTeam() { }
    
    public String name() {
        return name;
    }
    
    public Attributes attributes() {
        return attributes;
    }

    public static class Attributes {
        
        @SerializedName("uID")
        private String uId;
        
        public Attributes() { }
        
        public String uId() {
            return uId;
        }
    }
}
