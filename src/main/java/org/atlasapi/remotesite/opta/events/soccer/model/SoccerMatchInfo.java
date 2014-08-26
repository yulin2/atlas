package org.atlasapi.remotesite.opta.events.soccer.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gson.annotations.SerializedName;


public class SoccerMatchInfo {
    
    @SerializedName("Date")
    private MatchDate date;
    @SerializedName("TZ")
    private String timeZone;
    @SerializedName("@attributes")
    private MatchInfoAttributes attributes;
    
    public SoccerMatchInfo() { }
    
    public MatchDate date() {
        return date;
    }
    
    public String timeZone() {
        return timeZone;
    }
    
    public MatchInfoAttributes attributes() {
        return attributes;
    }

    public static class MatchInfoAttributes {
        
        @SerializedName("MatchDay")
        private String matchDay;
        @SerializedName("MatchType")
        private String matchType;
        @SerializedName("MatchWinner")
        private String matchWinner;
        @SerializedName("Period")
        private String period;
        @SerializedName("Venue_id")
        private String venueId;
        
        public MatchInfoAttributes() { }
        
        public String matchDay() {
            return matchDay;
        }
        
        public String matchType() {
            return matchType;
        }
        
        public String matchWinner() {
            return matchWinner;
        }

        public String period() {
            return period;
        }

        public String venueId() {
            return venueId;
        }
    }
    
    public static class MatchDate {
        
        private String date;
        
        public MatchDate(String date) { 
            this.date = checkNotNull(date);
        }
        
        public String date() {
            return date;
        }
    }
}
