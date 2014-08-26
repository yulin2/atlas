package org.atlasapi.remotesite.opta.events.sports.model;

import java.util.List;

import org.atlasapi.remotesite.opta.events.model.OptaMatch;

import com.google.gson.annotations.SerializedName;


public class OptaFixture implements OptaMatch {
    
    @SerializedName("@attributes")
    private FixtureAttributes attributes;
    @SerializedName("team")
    private List<OptaFixtureTeam> teams;
    
    public OptaFixture() { }
    
    public FixtureAttributes attributes() {
        return attributes;
    }
    
    public List<OptaFixtureTeam> teams() {
        return teams;
    }
        
}
