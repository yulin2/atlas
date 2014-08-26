package org.atlasapi.remotesite.opta.events.soccer.model;

import java.util.List;

import org.atlasapi.remotesite.opta.events.model.OptaMatch;

import com.google.gson.annotations.SerializedName;


public class SoccerMatchData implements OptaMatch {
    
    @SerializedName("MatchInfo")
    private SoccerMatchInfo matchInformation;
    @SerializedName("Stat")
    private List<SoccerStats> stats;
    @SerializedName("TeamData")
    private List<SoccerTeamData> teamData;
    @SerializedName("@attributes")
    private MatchDataAttributes attributes;
    
    public SoccerMatchData() { }
    
    public SoccerMatchInfo matchInformation() {
        return matchInformation;
    }
    
    public List<SoccerStats> stats() {
        return stats;
    }
    
    public List<SoccerTeamData> teamData() {
        return teamData;
    }
    
    public MatchDataAttributes attributes() {
        return attributes;
    }
}
