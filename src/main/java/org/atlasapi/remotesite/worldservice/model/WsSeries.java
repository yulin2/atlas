package org.atlasapi.remotesite.worldservice.model;

public class WsSeries {
    
    public static Builder wsSeriesBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private String seriesId;
        private String seriesTitle;
        private String strand;
        private String genreCode;
        private String ibmsSeriesBr;
        private String lastAmendTimestamp;
        private String timestamp;
        
        public Builder withSeriesId(String seriesId) {
            this.seriesId = seriesId;
            return this;
        }
        
        public Builder withSeriesTitle(String seriesTitle) {
            this.seriesTitle = seriesTitle;
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
        
        public Builder withIbmsSeriesBr(String ibmsSeriesBr) {
            this.ibmsSeriesBr = ibmsSeriesBr;
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
        
        public WsSeries build() {
            WsSeries series = new WsSeries();
            series.seriesId = seriesId;
            series.seriesTitle = seriesTitle;
            series.strand = strand;
            series.genreCode = genreCode;
            series.ibmsSeriesBr = ibmsSeriesBr;
            series.lastAmendTimestamp = lastAmendTimestamp;
            series.timestamp = timestamp;
            return series;
        }

    }
    
    private WsSeries() {
    }
    
    private String seriesId;
    private String seriesTitle;
    private String strand;
    private String genreCode;
    private String ibmsSeriesBr;
    private String lastAmendTimestamp;
    private String timestamp;

    public String getSeriesId() {
        return seriesId;
    }

    public String getSeriesTitle() {
        return seriesTitle;
    }

    public String getStrand() {
        return strand;
    }

    public String getGenreCode() {
        return genreCode;
    }

    public String getIbmsSeriesBr() {
        return ibmsSeriesBr;
    }

    public String getLastAmendTimestamp() {
        return lastAmendTimestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
