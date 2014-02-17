package org.atlasapi.remotesite.rovi.program;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.rovi.RoviConstants.ENGLISH_LANG;

import org.atlasapi.remotesite.rovi.ActionType;
import org.atlasapi.remotesite.rovi.KeyedActionLine;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.joda.time.Duration;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class RoviProgramLine implements KeyedActionLine<String> {
    
    private final String programId;
    private final String language;
    private final ActionType actionType;
    
    private final Optional<RoviShowType> showType;
    private final Optional<String> seriesId;
    private final Optional<String> seasonId;
    private final Optional<String> titleParentId;
    private final Optional<String> longTitle;
    private final Optional<String> episodeTitle;
    private final Optional<String> episodeNumber;
    private final Optional<Duration> duration;
    private final Optional<Integer> releaseYear;

    private RoviProgramLine(RoviShowType showType, String programId, String seriesId,
            String seasonId, String titleParentId, String longTitle, String episodeTitle,
            String episodeNumber, Duration duration, String language, Integer releaseYear, ActionType actionType) {
        this.programId = checkNotNull(programId);
        this.actionType = checkNotNull(actionType);
        this.language = Objects.firstNonNull(language, ENGLISH_LANG);

        this.showType = Optional.fromNullable(showType);
        this.seriesId = Optional.fromNullable(seriesId);
        this.seasonId = Optional.fromNullable(seasonId);
        this.titleParentId = Optional.fromNullable(titleParentId);
        this.longTitle = Optional.fromNullable(longTitle);
        this.episodeTitle = Optional.fromNullable(episodeTitle);
        this.episodeNumber = Optional.fromNullable(episodeNumber);
        this.duration = Optional.fromNullable(duration);
        this.releaseYear = Optional.fromNullable(releaseYear);
    }



    public Optional<RoviShowType> getShowType() {
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
    
    public Optional<String> getLongTitle() {
        return longTitle;
    }
    
    public Optional<String> getEpisodeTitle() {
        return episodeTitle;
    }
    
    public Optional<String> getEpisodeNumber() {
        return episodeNumber;
    }
    
    public Optional<Duration> getDuration() {
        return duration;
    }

    public String getLanguage() {
        return language;
    }
    
    @Override
    public String getKey() {
        return programId;
    }

    public Optional<Integer> getReleaseYear() {
        return releaseYear;
    }
    
    @Override
    public ActionType getActionType() {
        return actionType;
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
        private Integer releaseYear;
        private ActionType actionType;
        
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
        
        public Builder withReleaseYear(Integer releaseYear) {
            this.releaseYear = releaseYear;
            return this;
        }
        
        public Builder withActionType(ActionType actionType) {
            this.actionType = actionType;
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
                    language,
                    releaseYear,
                    actionType);
        }
    }

}
