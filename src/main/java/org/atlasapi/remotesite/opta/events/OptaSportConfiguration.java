package org.atlasapi.remotesite.opta.events;

import static com.google.api.client.util.Preconditions.checkNotNull;


public class OptaSportConfiguration {
    
    private final String feedType;
    private final String competition;
    private final String seasonId;
    
    public static Builder builder() {
        return new Builder();
    }
    
    private OptaSportConfiguration(String feedType, String competition, String seasonId) {
        this.feedType = checkNotNull(feedType);
        this.competition = checkNotNull(competition);
        this.seasonId = checkNotNull(seasonId);
    }
    
    public String feedType() {
        return feedType;
    }
    
    public String competition() {
        return competition;
    }
    
    public String seasonId() {
        return seasonId;
    }

    public static class Builder {
        
        private String feedType;
        private String competition;
        private String seasonId;
        
        public OptaSportConfiguration build() {
            return new OptaSportConfiguration(feedType, competition, seasonId);
        }
        
        private Builder() { }
        
        public Builder withFeedType(String feedType) {
            this.feedType = feedType;
            return this;
        }
        
        public Builder withCompetition(String competition) {
            this.competition = competition;
            return this;
        }
        
        public Builder withSeasonId(String seasonId) {
            this.seasonId = seasonId;
            return this;
        }
    }
}
