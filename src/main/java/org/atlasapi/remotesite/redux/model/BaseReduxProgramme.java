package org.atlasapi.remotesite.redux.model;

public class BaseReduxProgramme {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final BaseReduxProgramme prog;

        public Builder() {
            this.prog = new BaseReduxProgramme();
        }
        
        protected BaseReduxProgramme getProg() {
            return prog;
        }

        public Builder withDiskref(String diskref) {
            getProg().diskref = diskref;
            return this;
        }

        public Builder withService(String service) {
            getProg().service = service;
            return this;
        }

        public Builder withVariant(String variant) {
            getProg().variant = variant;
            return this;
        }

        public Builder withDate(String date) {
            getProg().date = date;
            return this;
        }

        public Builder withTime(String time) {
            getProg().time = time;
            return this;
        }

        public Builder withDuration(String duration) {
            getProg().duration = duration;
            return this;
        }

        public Builder withUri(String uri) {
            getProg().uri = uri;
            return this;
        }

        public Builder withSeriesUri(String seriesUri) {
            getProg().seriesUri = seriesUri;
            return this;
        }

        public Builder withCanonical(String canonical) {
            getProg().canonical = canonical;
            return this;
        }

        public Builder withDepiction(String depiction) {
            getProg().depiction = depiction;
            return this;
        }

        public Builder withTitle(String title) {
            getProg().title = title;
            return this;
        }

        public Builder withDescription(String description) {
            getProg().description = description;
            return this;
        }

        public Builder withSubtitles(Boolean subtitles) {
            getProg().subtitles = subtitles;
            return this;
        }

        public Builder withSigned(Boolean signed) {
            getProg().signed = signed;
            return this;
        }

        public Builder withHd(Boolean hd) {
            getProg().hd = hd;
            return this;
        }

        public Builder withRepeat(Boolean repeat) {
            getProg().repeat = repeat;
            return this;
        }

        public Builder withPcrid(String pcrid) {
            getProg().pcrid = pcrid;
            return this;
        }

        public Builder withScrid(String scrid) {
            getProg().scrid = scrid;
            return this;
        }

        public Builder withWhen(String when) {
            getProg().when = when;
            return this;
        }

        public Builder withId(String id) {
            getProg().id = id;
            return this;
        }

        public Builder withFilename(String filename) {
            getProg().filename = filename;
            return this;
        }

        public Builder withAd(Boolean ad) {
            getProg().ad = ad;
            return this;
        }
        
    }
    
    private String diskref;
    private String service;
    private String variant;
    private String date;
    private String time;
    private String duration;
    private String uri;
    private String seriesUri;
    private String canonical;
    private String depiction;
    private String title;
    private String description;
    private Boolean subtitles;
    private Boolean signed;
    private Boolean hd;
    private Boolean repeat;
    private String pcrid;
    private String scrid;
    private String when;
    private String id;
    private String filename;
    private Boolean ad;

    public String getDiskref() {
        return diskref;
    }

    public String getService() {
        return service;
    }

    public String getVariant() {
        return variant;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDuration() {
        return duration;
    }

    public String getUri() {
        return uri;
    }

    public String getSeriesUri() {
        return seriesUri;
    }

    public String getCanonical() {
        return canonical;
    }

    public String getDepiction() {
        return depiction;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getSubtitles() {
        return subtitles;
    }

    public Boolean getSigned() {
        return signed;
    }

    public Boolean getHd() {
        return hd;
    }

    public Boolean getRepeat() {
        return repeat;
    }

    public String getPcrid() {
        return pcrid;
    }

    public String getScrid() {
        return scrid;
    }

    public String getWhen() {
        return when;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public Boolean getAd() {
        return ad;
    }
}
