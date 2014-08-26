package org.atlasapi.remotesite.opta.events.soccer.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class OptaSoccerDocument {
    
    @SerializedName("MatchData")
    private List<SoccerMatchData> matchData;
    @SerializedName("TimingTypes")
    private SoccerTimingType timingType;
    @SerializedName("Team")
    private List<SoccerTeam> teams;
    @SerializedName("@attributes")
    private SoccerFeedAttributes attributes;
    
    public OptaSoccerDocument() { }
    
    public List<SoccerMatchData> matchData() {
        return matchData;
    }
    
    public SoccerTimingType timingType() {
        return timingType;
    }
    
    public List<SoccerTeam> teams() {
        return teams;
    }
    
    public SoccerFeedAttributes attributes() {
        return attributes;
    }
}
