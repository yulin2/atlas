package org.atlasapi.remotesite.rovi.series;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;
import org.joda.time.DateTime;

import com.google.common.base.Optional;

public class RoviTheatricalReleaseDatesLine implements KeyedLine<String> {

    private final String programId;
    private final DateTime releaseDate;
    private final String releaseCountry;
    private final Optional<String> releaseType;

    private RoviTheatricalReleaseDatesLine(String programId, DateTime releaseDate,
            String releaseCountry, String releaseType) {
        this.programId = checkNotNull(programId);
        this.releaseDate = checkNotNull(releaseDate);
        this.releaseCountry = checkNotNull(releaseCountry);
        this.releaseType = Optional.fromNullable(releaseType);
    }

    public String getProgramId() {
        return programId;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    public String getReleaseCountry() {
        return releaseCountry;
    }

    public Optional<String> getReleaseType() {
        return releaseType;
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
        private DateTime releaseDate;
        private String releaseCountry;
        private String releaseType;
        
        public Builder withProgramId(String programId) {
            this.programId = programId;
            return this;
        }
        
        public Builder withReleaseDate(DateTime releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }
        
        public Builder withReleaseCountry(String releaseCountry) {
            this.releaseCountry = releaseCountry;
            return this;
        }
        
        public Builder withReleaseType(String releaseType) {
            this.releaseType = releaseType;
            return this;
        }
        
        public RoviTheatricalReleaseDatesLine build() {
            return new RoviTheatricalReleaseDatesLine(
                    programId,
                    releaseDate,
                    releaseCountry,
                    releaseType);
        }
    }

}
