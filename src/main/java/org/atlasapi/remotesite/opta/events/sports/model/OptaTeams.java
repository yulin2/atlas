package org.atlasapi.remotesite.opta.events.sports.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class OptaTeams {

    @SerializedName("team")
    private List<OptaSportsTeam> teams;
    
    public OptaTeams() { }
    
    public List<OptaSportsTeam> teams() {
        return teams;
    }
}
