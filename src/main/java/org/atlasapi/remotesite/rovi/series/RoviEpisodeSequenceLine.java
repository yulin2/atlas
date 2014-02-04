package org.atlasapi.remotesite.rovi.series;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;

public class RoviEpisodeSequenceLine implements KeyedLine<String> {

    private final String seriesId;
    private final String seasonProgramId;
    private final String programId;
    private final String episodeTitle;
    private final Integer episodeSeasonSequence;

    private RoviEpisodeSequenceLine(String seriesId, String seasonProgramId, String programId,
            String episodeTitle, Integer episodeSeasonSequence) {
        
        this.seriesId = checkNotNull(seriesId);
        this.seasonProgramId = checkNotNull(seasonProgramId);
        this.programId = checkNotNull(programId);
        this.episodeTitle = checkNotNull(episodeTitle);
        this.episodeSeasonSequence = checkNotNull(episodeSeasonSequence);
    }

    public String getSeriesId() {
        return seriesId;
    }

    public String getSeasonProgramId() {
        return seasonProgramId;
    }

    public String getProgramId() {
        return programId;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public Integer getEpisodeSeasonSequence() {
        return episodeSeasonSequence;
    }

    @Override
    public String getKey() {
        return programId;
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

        public RoviEpisodeSequenceLine build() {
            return new RoviEpisodeSequenceLine(
                    seriesId,
                    seasonProgramId,
                    programId,
                    episodeTitle,
                    episodeSeasonSequence);
        }

    }

}
