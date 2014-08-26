package org.atlasapi.remotesite.opta.events.soccer.model;

import com.google.gson.annotations.SerializedName;


public class SoccerFeedAttributes {
        
    @SerializedName("Type")
    private String type;
    @SerializedName("competition_code")
    private String competitionCode;
    @SerializedName("competition_id")
    private String competitionId;
    @SerializedName("competition_name")
    private String competitionName;
    @SerializedName("game_system_id")
    private String gameSystemId;
    @SerializedName("season_id")
    private String seasonId;
    @SerializedName("season_name")
    private String seasonName;
    private String timestamp;

    public SoccerFeedAttributes() { }

    public String type() {
        return type;
    }

    public String competitionCode() {
        return competitionCode;
    }

    public String competitionId() {
        return competitionId;
    }

    public String competitionName() {
        return competitionName;
    }

    public String gameSystemId() {
        return gameSystemId;
    }

    public String seasonId() {
        return seasonId;
    }
    
    public String seasonName() {
        return seasonName;
    }
    
    public String timestamp() {
        return timestamp;
    }
}
