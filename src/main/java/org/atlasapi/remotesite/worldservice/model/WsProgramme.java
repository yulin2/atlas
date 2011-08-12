package org.atlasapi.remotesite.worldservice.model;

public class WsProgramme {

    public static Builder wsProgrammeBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String progId;
        private String seriesId;
        private String episodeTitle;
        private String synopsis;
        private String strand;
        private String genreCode;
        private String episodeNo;
        private String totalNoEpisodes;
        private String progDuration;
        private String firstTxDate;
        private String ibmsProgrammeBR;
        private String lastAmendTimestamp;
        private String timestamp;

        public Builder withProgId(String progId) {
            this.progId = progId;
            return this;
        }

        public Builder withSeriesId(String seriesId) {
            this.seriesId = seriesId;
            return this;
        }

        public Builder withEpisodeTitle(String episodeTitle) {
            this.episodeTitle = episodeTitle;
            return this;
        }

        public Builder withSynopsis(String synopsis) {
            this.synopsis = synopsis;
            return this;
        }

        public Builder withStrand(String strand) {
            this.strand = strand;
            return this;
        }

        public Builder withGenreCode(String genreCode) {
            this.genreCode = genreCode;
            return this;
        }

        public Builder withEpisodeNo(String episodeNo) {
            this.episodeNo = episodeNo;
            return this;
        }

        public Builder withTotalNoEpisodes(String totalNoEpisodes) {
            this.totalNoEpisodes = totalNoEpisodes;
            return this;
        }

        public Builder withProgDuration(String progDuration) {
            this.progDuration = progDuration;
            return this;
        }

        public Builder withFirstTxDate(String firstTxDate) {
            this.firstTxDate = firstTxDate;
            return this;
        }

        public Builder withIbmsProgrammeBR(String ibmsProgrammeBR) {
            this.ibmsProgrammeBR = ibmsProgrammeBR;
            return this;
        }

        public Builder withLastAmendTimestamp(String lastAmendTimestamp) {
            this.lastAmendTimestamp = lastAmendTimestamp;
            return this;
        }

        public Builder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public WsProgramme build() {
            WsProgramme programme = new WsProgramme();
            programme.progId = progId;
            programme.seriesId = seriesId;
            programme.episodeTitle = episodeTitle;
            programme.synopsis = synopsis;
            programme.strand = strand;
            programme.genreCode = genreCode;
            programme.episodeNo = episodeNo;
            programme.totalNoEpisodes = totalNoEpisodes;
            programme.progDuration = progDuration;
            programme.firstTxDate = firstTxDate;
            programme.ibmsProgrammeBR = ibmsProgrammeBR;
            programme.lastAmendTimestamp = lastAmendTimestamp;
            programme.timestamp = timestamp;
            return programme;
        }
    }
    
    private WsProgramme() {}
    
    private String progId;
    private String seriesId;
    private String episodeTitle;
    private String synopsis;
    private String strand;
    private String genreCode;
    private String episodeNo;
    private String totalNoEpisodes;
    private String progDuration;
    private String firstTxDate;
    private String ibmsProgrammeBR;
    private String lastAmendTimestamp;
    private String timestamp;

    public String getProgId() {
        return progId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getStrand() {
        return strand;
    }

    public String getGenreCode() {
        return genreCode;
    }

    public String getEpisodeNo() {
        return episodeNo;
    }

    public String getTotalNoEpisodes() {
        return totalNoEpisodes;
    }

    public String getProgDuration() {
        return progDuration;
    }

    public String getFirstTxDate() {
        return firstTxDate;
    }

    public String getIbmsProgrammeBR() {
        return ibmsProgrammeBR;
    }

    public String getLastAmendTimestamp() {
        return lastAmendTimestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
