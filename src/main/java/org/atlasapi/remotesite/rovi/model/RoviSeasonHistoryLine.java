package org.atlasapi.remotesite.rovi.model;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.indexing.KeyedActionLine;

import com.google.common.base.Optional;


public class RoviSeasonHistoryLine implements KeyedActionLine<String>{

    private final String seasonHistoryId;
    private final ActionType actionType;

    private final Optional<String> seriesId;
    private final Optional<String> seasonProgramId;
    private final Optional<Integer> seasonNumber;
    private final Optional<String> seasonName;
    
    private RoviSeasonHistoryLine(String seriesId, String seasonProgramId, Integer seasonNumber, String seasonName, String seasonHistoryId, ActionType actionType) {
        this.seasonHistoryId = checkNotNull(seasonHistoryId);
        this.actionType = checkNotNull(actionType);
        
        this.seriesId = Optional.fromNullable(seriesId);
        this.seasonProgramId = Optional.fromNullable(seasonProgramId);
        this.seasonNumber = Optional.fromNullable(seasonNumber);
        this.seasonName = Optional.fromNullable(seasonName);
    }
    
    public Optional<String> getSeriesId() {
        return seriesId;
    }
    
    public Optional<String> getSeasonProgramId() {
        return seasonProgramId;
    }
    
    public Optional<Integer> getSeasonNumber() {
        return seasonNumber;
    }
    
    public Optional<String> getSeasonName() {
        return seasonName;
    }
    
    public String getSeasonHistoryId() {
        return seasonHistoryId;
    }

    @Override
    public String getKey() {
        return seasonProgramId.get();
    }
    
    @Override
    public ActionType getActionType() {
        return actionType;
    }

    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String seriesId;
        private String seasonProgramId;
        private Integer seasonNumber;
        private String seasonName;
        private String seasonHistoryId;
        private ActionType actionType;
        
        public Builder withSeriesId(String seriesId) {
            this.seriesId = seriesId;
            return this;
        }
        
        public Builder withSeasonProgramId(String seasonProgramId) {
            this.seasonProgramId = seasonProgramId;
            return this;
        }
        
        public Builder withSeasonNumber(Integer seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }
        
        public Builder withSeasonName(String seasonName) {
            this.seasonName = seasonName;
            return this;
        }
        
        public Builder withSeasonHistoryId(String seasonHistoryId) {
            this.seasonHistoryId = seasonHistoryId;
            return this;
        }  
        
        public Builder withActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }
        
        public RoviSeasonHistoryLine build() {
            return new RoviSeasonHistoryLine(
                    seriesId,
                    seasonProgramId,
                    seasonNumber,
                    seasonName,
                    seasonHistoryId,
                    actionType);
        }
        
    }

}
