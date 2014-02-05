package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.remotesite.rovi.KeyedLine;
import org.atlasapi.remotesite.rovi.RoviConstants;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.joda.time.Duration;

import com.google.common.base.Optional;

public class RoviProgramLine implements KeyedLine<String>{

    private final RoviShowType showType;
    private final String programId;
    private final Optional<String> seriesId;
    private final Optional<String> seasonId;
    private final Optional<String> titleParentId;
    private final String longTitle;
    private final Optional<String> episodeTitle;
    private final Optional<String> episodeNumber;
    private final Duration duration;
    private final String language;

    private RoviProgramLine(RoviShowType showType, String programId, String seriesId,
            String seasonId, String titleParentId, String longTitle, String episodeTitle,
            String episodeNumber, Duration duration, String language) {
        this.showType = showType;
        this.programId = programId;
        this.seriesId = Optional.fromNullable(seriesId);
        this.seasonId = Optional.fromNullable(seasonId);
        this.titleParentId = Optional.fromNullable(titleParentId);
        this.longTitle = longTitle;
        this.episodeTitle = Optional.fromNullable(episodeTitle);
        this.episodeNumber = Optional.fromNullable(episodeNumber);
        this.duration = duration;
        this.language = language;
    }

    public RoviShowType getShowType() {
        return showType;
    }
    
    public String getProgramId() {
        return programId;
    }
    
    public Optional<String> getSeriesId() {
        return seriesId;
    }
    
    public Optional<String> getSeasonId() {
        return seasonId;
    }
    
    public Optional<String> getTitleParentId() {
        return titleParentId;
    }
    
    public String getLongTitle() {
        return longTitle;
    }
    
    public Optional<String> getEpisodeTitle() {
        return episodeTitle;
    }
    
    public Optional<String> getEpisodeNumber() {
        return episodeNumber;
    }
    
    public Duration getDuration() {
        return duration;
    }

    public String getLanguage() {
        return language;
    }
    
    @Override
    public String getKey() {
        return programId;
    }

    public boolean isEnglish() {
        return getLanguage().equalsIgnoreCase(RoviConstants.ENGLISH_LANG);
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private RoviShowType showType;
        private String programId;
        private String seriesId;
        private String seasonId;
        private String titleParentId;
        private String longTitle;
        private String episodeTitle;
        private String episodeNumber;
        private Duration duration;
        private String language;
        
        public Builder withShowType(RoviShowType showType) {
            this.showType = showType;
            return this;
        }
        
        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }
        
        public Builder withProgramId(String programId) {
            this.programId = programId;
            return this;
        }

        public Builder withSeriesId(String seriesId) {
            this.seriesId = seriesId;
            return this;
        }
        
        public Builder withSeasonId(String seasonId) {
            this.seasonId = seasonId;
            return this;
        }
        
        public Builder withTitleParentId(String titleParentId) {
            this.titleParentId = titleParentId;
            return this;
        }
        
        public Builder withLongTitle(String longTitle) {
            this.longTitle = longTitle;
            return this;
        }

        public Builder withEpisodeTitle(String episodeTitle) {
            this.episodeTitle = episodeTitle;
            return this;
        }
        
        public Builder withEpisodeNumber(String episodeNumber) {
            this.episodeNumber = episodeNumber;
            return this;
        }
        
        public Builder withDuration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public final RoviProgramLine build() {
            return new RoviProgramLine(
                    showType,
                    programId,
                    seriesId,
                    seasonId,
                    titleParentId,
                    longTitle,
                    episodeTitle,
                    episodeNumber,
                    duration,
                    language);
        }
    }

}
