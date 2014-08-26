package org.atlasapi.remotesite.opta.events.sports.model;

import org.atlasapi.remotesite.opta.events.model.OptaTeam;

import com.google.gson.annotations.SerializedName;


public class OptaSportsTeam implements OptaTeam {

    @SerializedName("@value")
    private String value;
    @SerializedName("@attributes")
    private Attributes attributes;
    
    public OptaSportsTeam() { }
    
    public String value() {
        return value;
    }
    
    public Attributes attributes() {
        return attributes;
    }

    public static class Attributes {
        
        private String id;
        private String name;
        
        public Attributes() { }
        
        public String id() {
            return id;
        }
        
        public String name() {
            return name;
        }
    }
}
