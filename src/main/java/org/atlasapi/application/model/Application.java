package org.atlasapi.application.model;

import org.atlasapi.application.ApplicationCredentials;
import org.atlasapi.media.common.Id;
import org.joda.time.DateTime;

public class Application {

    private final Id id;
    private final String slug; // Kept to enable creation of compatible entries
                               // for 3.0
    private final String title;
    private final DateTime created;
    private final ApplicationCredentials credentials;
    private final ApplicationSources sources;

    private Application(Builder builder) {
        this.id = builder.id;
        this.slug = builder.slug;
        this.title = builder.title;
        this.created = builder.created;
        this.credentials = builder.credentials;
        this.sources = builder.sources;
    }

    public Id getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public DateTime getCreated() {
        return created;
    }

    public ApplicationCredentials getCredentials() {
        return credentials;
    }

    public ApplicationSources getSources() {
        return sources;
    }

    public Builder copy() {
        return builder()
                .withId(this.getId())
                .withSlug(this.getSlug())
                .withTitle(this.getTitle())
                .withCreated(this.getCreated())
                .withCredentials(this.getCredentials())
                .withSources(this.getSources());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Id id;
        private String slug;
        private String title;
        private DateTime created;
        private ApplicationCredentials credentials;
        private ApplicationSources sources;

        public Builder() {

        }

        public Builder withId(Id id) {
            this.id = id;
            return this;
        }

        public Builder withSlug(String slug) {
            this.slug = slug;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withCreated(DateTime created) {
            this.created = created;
            return this;
        }

        public Builder withCredentials(ApplicationCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder withSources(ApplicationSources sources) {
            this.sources = sources;
            return this;
        }

        public Application build() {
            return new Application(this);
        }
    }

}
