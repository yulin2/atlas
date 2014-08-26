package org.atlasapi.remotesite.opta.events.soccer.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class SoccerTeamData {

    @SerializedName("Goal")
    private List<SoccerGoal> goals;
    @SerializedName("@attributes")
    private TeamDataAttributes attributes;

    public SoccerTeamData() { }
    
    public List<SoccerGoal> goals() {
        return goals;
    }
    
    public TeamDataAttributes attributes() {
        return attributes;
    }
    
    public static class TeamDataAttributes {
        
        @SerializedName("HalfScore")
        private String halfScore;
        @SerializedName("Score")
        private String score;
        @SerializedName("Side")
        private String side;
        @SerializedName("TeamRef")
        private String teamRef;
        
        public TeamDataAttributes() { }
        
        public String halfScore() {
            return halfScore;
        }
        
        public String score() {
            return score;
        }
        
        public String side() {
            return side;
        }
        
        public String teamRef() {
            return teamRef;
        }
    }
}
