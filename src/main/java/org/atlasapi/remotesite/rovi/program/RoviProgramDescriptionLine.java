package org.atlasapi.remotesite.rovi.program;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;

import com.google.common.base.Optional;

public class RoviProgramDescriptionLine implements KeyedLine<String>{

    private final String programId;
    private final Optional<String> sourceId;
    private final String descriptionCulture;
    private final String descriptionType;
    private final String description;

    private RoviProgramDescriptionLine(String programId, String sourceId,
            String descriptionCulture, String descriptionType, String description) {
        this.programId = checkNotNull(programId);
        this.sourceId = Optional.fromNullable(sourceId);
        this.descriptionCulture = checkNotNull(descriptionCulture);
        this.descriptionType = checkNotNull(descriptionType);
        this.description = checkNotNull(description);
    }

    public String getProgramId() {
        return programId;
    }

    public Optional<String> getSourceId() {
        return sourceId;
    }

    public String getDescriptionCulture() {
        return descriptionCulture;
    }

    public String getDescriptionType() {
        return descriptionType;
    }

    public String getDescription() {
        return description;
    }
    
    @Override
    public String getKey() {
        return programId;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String programId;
        private String sourceId;
        private String descriptionCulture;
        private String descriptionType;
        private String description;

        public Builder withProgramId(String programId) {
            this.programId = programId;
            return this;
        }

        public Builder withSourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder withDescriptionCulture(String descriptionCulture) {
            this.descriptionCulture = descriptionCulture;
            return this;
        }

        public Builder withDescriptionType(String descriptionType) {
            this.descriptionType = descriptionType;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public RoviProgramDescriptionLine build() {
            return new RoviProgramDescriptionLine(
                    programId,
                    sourceId,
                    descriptionCulture,
                    descriptionType,
                    description);
        }

    }

}
