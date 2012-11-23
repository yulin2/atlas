package org.atlasapi.remotesite.netflix;

public enum NetflixMovieElement {
    TITLE("title"),
    RELEASE_YEAR("release_year"),
    LONG_SYNOPSIS("long_synopsis"),
    SHORT_SYNOPSIS("short_synopsis"),
    DURATION("duration"),
    GENRES("genres"),
    PEOPLE("people"),
    PARENTAL_ADVISORIES("parental_advisories"),
    URL("url");
    
    private String name;
    
    private NetflixMovieElement(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
