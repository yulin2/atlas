package org.atlasapi.remotesite.rovi.model;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.indexing.KeyedActionLine;

import com.google.common.base.Optional;

public class RoviProgramDescriptionLine implements KeyedActionLine<String>{

    private final String programId;
    private final Optional<String> sourceId;
    private final String descriptionCulture;
    private final String descriptionType;
    private final Optional<String> description;
    private final ActionType actionType;

    private RoviProgramDescriptionLine(String programId, String sourceId,
            String descriptionCulture, String descriptionType, String description, ActionType actionType) {
        this.programId = checkNotNull(programId);
        this.descriptionCulture = checkNotNull(descriptionCulture);
        this.descriptionType = checkNotNull(descriptionType);
        this.actionType = checkNotNull(actionType);

        this.sourceId = Optional.fromNullable(sourceId);
        this.description = Optional.fromNullable(description);
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

    public Optional<String> getDescription() {
        return description;
    }
    
    @Override
    public String getKey() {
        return programId;
    }
    
    @Override
    public ActionType getActionType() {
        return actionType;
    }
    
    public boolean isOfTypeAndDescriptionPresent(String descriptionType) {
        return descriptionType.equals(this.descriptionType) && this.description.isPresent();
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
        private ActionType actionType;

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
        
        public Builder withActionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public RoviProgramDescriptionLine build() {
            return new RoviProgramDescriptionLine(
                    programId,
                    sourceId,
                    descriptionCulture,
                    descriptionType,
                    description,
                    actionType);
        }

    }

}
