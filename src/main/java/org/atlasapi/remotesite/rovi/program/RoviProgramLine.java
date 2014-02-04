package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.remotesite.rovi.KeyedLine;
import org.atlasapi.remotesite.rovi.RoviConstants;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.joda.time.Duration;

import com.google.common.base.Optional;

public class RoviProgramLine implements KeyedLine<String>{

    private final RoviShowType showType;
    private final String programId;
    private final String seriesId;
    private final String seasonId;
    private final String variantParentId;
    private final String groupId;
    private final boolean isGroupLanguagePrimary;
    private final String longTitle;
    private final String episodeTitle;
    private final Optional<Long> episodeNumber;
    private final Duration duration;
    private final String language;

    private RoviProgramLine(RoviShowType showType, String programId, String seriesId,
            String seasonId, String variantParentId, String groupId,
            boolean isGroupLanguagePrimary, String longTitle, String episodeTitle, Long episodeNumber, Duration duration, String language) {
        this.showType = showType;
        this.programId = programId;
        this.seriesId = seriesId;
        this.seasonId = seasonId;
        this.variantParentId = variantParentId;
        this.groupId = groupId;
        this.isGroupLanguagePrimary = isGroupLanguagePrimary;
        this.longTitle = longTitle;
        this.episodeTitle = episodeTitle;
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
    
    public String getSeriesId() {
        return seriesId;
    }
    
    public String getSeasonId() {
        return seasonId;
    }
    
    public String getVariantParentId() {
        return variantParentId;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public boolean isGroupLanguagePrimary() {
        return isGroupLanguagePrimary;
    }
    
    public String getLongTitle() {
        return longTitle;
    }
    
    public String getEpisodeTitle() {
        return episodeTitle;
    }
    
    public Optional<Long> getEpisodeNumber() {
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

    public static class Builder {

        private RoviShowType showType;
        private String programId;
        private String seriesId;
        private String seasonId;
        private String variantParentId;
        private String groupId;
        private boolean isGroupLanguagePrimary;
        private String longTitle;
        private String episodeTitle;
        private Long episodeNumber;
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
        
        public Builder withVariantParentId(String variantParentId) {
            this.variantParentId = variantParentId;
            return this;
        }
        
        public Builder withGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }
        
        public Builder withIsGroupLanguagePrimary(boolean isGroupLanguagePrimary) {
            this.isGroupLanguagePrimary = isGroupLanguagePrimary;
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
        
        public Builder withEpisodeNumber(Long episodeNumber) {
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
                    variantParentId,
                    groupId,
                    isGroupLanguagePrimary,
                    longTitle,
                    episodeTitle,
                    episodeNumber,
                    duration,
                    language);
        }
    }

}
