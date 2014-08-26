package org.atlasapi.remotesite.opta.events.sports.model;

import com.google.gson.annotations.SerializedName;


public class FixtureAttributes {

    @SerializedName("comp_id")
    private String compId;
    @SerializedName("comp_name")
    private String compName;
    @SerializedName("game_date")
    private String gameDate;
    private String group;
    @SerializedName("group_name")
    private String groupName;
    private String id;
    private String leg;
    @SerializedName("live_scores")
    private String liveScores;
    @SerializedName("public")
    private String isPublic;
    private String round;
    @SerializedName("round_type_id")
    private String roundTypeId;
    @SerializedName("season_id")
    private String seasonId;
    private String stage;
    private String time;
    private String venue;
    @SerializedName("venue_id")
    private String venueId;
    
    public FixtureAttributes() { }

    public String compId() {
        return compId;
    }

    public String compName() {
        return compName;
    }

    public String gameDate() {
        return gameDate;
    }
    
    public String group() {
        return group;
    }
    
    public String groupName() {
        return groupName;
    }
    
    public String id() {
        return id;
    }
    
    public String leg() {
        return leg;
    }
    
    public String liveScores() {
        return liveScores;
    }
    
    public String isPublic() {
        return isPublic;
    }
    
    public String round() {
        return round;
    }
    
    public String roundTypeId() {
        return roundTypeId;
    }
    
    public String seasonId() {
        return seasonId;
    }
    
    public String stage() {
        return stage;
    }
    
    public String time() {
        return time;
    }
    
    public String venue() {
        return venue;
    }
    
    public String venueId() {
        return venueId;
    }
}
