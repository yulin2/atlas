package org.atlasapi.remotesite.opta.events.sports.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class OptaSportsFeed {

    private OptaFixtures fixtures;
    
    public OptaSportsFeed() { }
    
    public OptaFixtures fixtures() {
        return fixtures;
    }
    
    public static class OptaFixtures {
        
        @SerializedName("fixture")
        private List<OptaFixture> fixtures;
        private OptaTeams teams;
        
        public OptaFixtures() { }
        
        public List<OptaFixture> fixtures() {
            return fixtures;
        }
        
        public OptaTeams teams() {
            return teams;
        }
    }
}
