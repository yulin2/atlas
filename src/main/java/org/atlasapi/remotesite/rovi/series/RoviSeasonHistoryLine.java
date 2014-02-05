package org.atlasapi.remotesite.rovi.series;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;

import com.google.common.base.Optional;


public class RoviSeasonHistoryLine implements KeyedLine<String>{

    private final String seriesId;
    private final String seasonProgramId;
    private final Optional<Integer> seasonNumber;
    private final Optional<String> seasonName;
    private final String seasonHistoryId;
    
    private RoviSeasonHistoryLine(String seriesId, String seasonProgramId, Integer seasonNumber, String seasonName, String seasonHistoryId) {
        this.seriesId = checkNotNull(seriesId);
        this.seasonProgramId = checkNotNull(seasonProgramId);
        this.seasonNumber = Optional.fromNullable(seasonNumber);
        this.seasonName = Optional.fromNullable(seasonName);
        this.seasonHistoryId = checkNotNull(seasonHistoryId);
    }
    
    public String getSeriesId() {
        return seriesId;
    }
    
    public String getSeasonProgramId() {
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
        return seasonHistoryId;
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
        
        public RoviSeasonHistoryLine build() {
            return new RoviSeasonHistoryLine(
                    seriesId,
                    seasonProgramId,
                    seasonNumber,
                    seasonName,
                    seasonHistoryId);
        }
        
    }

}
