package org.atlasapi.remotesite.redux.model;

import java.util.Map;

public class FullReduxProgramme extends BaseReduxProgramme {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseReduxProgramme.Builder {

        private FullReduxProgramme prog;

        public Builder() {
            this.prog = new FullReduxProgramme();
        }

        @Override
        protected FullReduxProgramme getProg() {
            return prog;
        }
        
        public Builder withType(String type) {
            getProg().type = type;
            return this;
        }

        public Builder withSource(String source) {
            getProg().source = source;
            return this;
        }

        public Builder withServiceName(String serviceName) {
            getProg().serviceName = serviceName;
            return this;
        }

        public Builder withBroadcast(ReduxBroadcast broadcast) {
            getProg().broadcast = broadcast;
            return this;
        }

        public Builder withEpisode(ReduxEpisode episode) {
            getProg().episode = episode;
            return this;
        }

        public Builder withBrand(ReduxBrand brand) {
            getProg().brand = brand;
            return this;
        }

        public Builder withVersion(ReduxVersion version) {
            getProg().version = version;
            return this;
        }

        public Builder withKey(String key) {
            getProg().key = key;
            return this;
        }

        public Builder withMedia(Map<String, ReduxMedia> media) {
            getProg().media = media;
            return this;
        }

    }

    private String type;
    private String source;
    private String serviceName;
    private ReduxBroadcast broadcast;
    private ReduxEpisode episode;
    private ReduxBrand brand;
    private ReduxVersion version;
    private String key;
    private Map<String, ReduxMedia> media;

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ReduxBroadcast getBroadcast() {
        return broadcast;
    }

    public ReduxEpisode getEpisode() {
        return episode;
    }

    public ReduxBrand getBrand() {
        return brand;
    }

    public ReduxVersion getVersion() {
        return version;
    }

    public String getKey() {
        return key;
    }

    public Map<String, ReduxMedia> getMedia() {
        return media;
    }

}
