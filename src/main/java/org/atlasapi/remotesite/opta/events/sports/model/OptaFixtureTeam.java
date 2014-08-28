package org.atlasapi.remotesite.opta.events.sports.model;

import com.google.gson.annotations.SerializedName;


public class OptaFixtureTeam {

    @SerializedName("@attributes")
    private Attributes attributes;
    @SerializedName("@value")
    private String value;
    
    public OptaFixtureTeam() { }
    
    public Attributes attributes() {
        return attributes;
    }
    
    public String value() {
        return value;
    }
    
    public static class Attributes {
        
        @SerializedName("home_or_away")
        private String homeOrAway;
        @SerializedName("team_id")
        private String teamId;
        
        public Attributes() { }
        
        public String homeOrAway() {
            return homeOrAway;
        }
        
        public String teamId() {
            return teamId;
        }
    }
}
