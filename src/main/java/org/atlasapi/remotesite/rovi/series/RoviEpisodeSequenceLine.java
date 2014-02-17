package org.atlasapi.remotesite.rovi.series;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.ActionType;
import org.atlasapi.remotesite.rovi.KeyedActionLine;

import com.google.common.base.Optional;

public class RoviEpisodeSequenceLine implements KeyedActionLine<String> {

    private final Optional<String> seriesId;
    private final Optional<String> seasonProgramId;
    private final String programId;
    private final Optional<String> episodeTitle;
    private final Optional<Integer> episodeSeasonSequence;
    private final Optional<Integer> episodeSeasonNumber;
    private final ActionType actionType;

    private RoviEpisodeSequenceLine(String seriesId, String seasonProgramId, String programId,
            String episodeTitle, Integer episodeSeasonSequence, Integer episodeSeasonNumber, ActionType actionType) {
        
        this.programId = checkNotNull(programId);
        this.actionType = checkNotNull(actionType);

        this.seriesId = Optional.fromNullable(seriesId);
        this.seasonProgramId = Optional.fromNullable(seasonProgramId);
        this.episodeTitle = Optional.fromNullable(episodeTitle);
        this.episodeSeasonSequence = Optional.fromNullable(episodeSeasonSequence);
        this.episodeSeasonNumber = Optional.fromNullable(episodeSeasonNumber);
    }

    public Optional<String> getSeriesId() {
        return seriesId;
    }

    public Optional<String> getSeasonProgramId() {
        return seasonProgramId;
    }

    public String getProgramId() {
        return programId;
    }

    public Optional<String> getEpisodeTitle() {
        return episodeTitle;
    }

    public Optional<Integer> getEpisodeSeasonSequence() {
        return episodeSeasonSequence;
    }
    
    public Optional<Integer> getEpisodeSeasonNumber() {
        return episodeSeasonNumber;
    }

    @Override
    public String getKey() {
        return programId;
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
        private String programId;
        private String episodeTitle;
        private Integer episodeSeasonSequence;
        private Integer episodeSeasonNumber;
        private ActionType actionType;

        public Builder withSeriesId(String seriesId) {
            this.seriesId = seriesId;
            return this;
        }

        public Builder withSeasonProgramId(String seasonProgramId) {
            this.seasonProgramId = seasonProgramId;
            return this;
        }

        public Builder withProgramId(String programId) {
            this.programId = programId;
            return this;
        }

        public Builder withEpisodeTitle(String episodeTitle) {
            this.episodeTitle = episodeTitle;
            return this;
        }

        public Builder withEpisodeSeasonSequence(Integer episodeSeasonSequence) {
            this.episodeSeasonSequence = episodeSeasonSequence;
            return this;
        }
        
        public Builder withEpisodeSeasonNumber(Integer episodeSeasonNumber) {
            this.episodeSeasonNumber = episodeSeasonNumber;
            return this;
        }
        
        public Builder withActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public RoviEpisodeSequenceLine build() {
            return new RoviEpisodeSequenceLine(
                    seriesId,
                    seasonProgramId,
                    programId,
                    episodeTitle,
                    episodeSeasonSequence,
                    episodeSeasonNumber,
                    actionType);
        }

    }

}
