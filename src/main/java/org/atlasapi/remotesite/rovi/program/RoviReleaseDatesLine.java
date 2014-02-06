package org.atlasapi.remotesite.rovi.program;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;

public class RoviReleaseDatesLine implements KeyedLine<String> {

    private final String programId;
    private final LocalDate releaseDate;
    private final String releaseCountry;
    private final Optional<String> releaseType;

    private RoviReleaseDatesLine(String programId, LocalDate releaseDate,
            String releaseCountry, String releaseType) {
        this.programId = checkNotNull(programId);
        this.releaseDate = checkNotNull(releaseDate);
        this.releaseCountry = checkNotNull(releaseCountry);
        this.releaseType = Optional.fromNullable(releaseType);
    }

    public String getProgramId() {
        return programId;
    }

    public LocalDate getReleaseDate() {
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
        private LocalDate releaseDate;
        private String releaseCountry;
        private String releaseType;
        
        public Builder withProgramId(String programId) {
            this.programId = programId;
            return this;
        }
        
        public Builder withReleaseDate(LocalDate releaseDate) {
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
        
        public RoviReleaseDatesLine build() {
            return new RoviReleaseDatesLine(
                    programId,
                    releaseDate,
                    releaseCountry,
                    releaseType);
        }
    }

}
