package org.atlasapi.remotesite.rovi.series;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.KeyedLine;

import com.google.common.base.Optional;

public class RoviSeriesLine implements KeyedLine<String>{

    private final String seriesId;
    private final String fullTitle;
    private final Optional<String> synopsis;
    
    public RoviSeriesLine(String seriesId, String fullTitle, String synopsis) {
        this.seriesId = checkNotNull(seriesId);
        this.fullTitle = checkNotNull(fullTitle);
        this.synopsis = Optional.fromNullable(synopsis);
    }
    
    public String getSeriesId() {
        return seriesId;
    }
    
    public String getFullTitle() {
        return fullTitle;
    }
    
    public Optional<String> getSynopsis() {
        return synopsis;
    }

    @Override
    public String getKey() {
        return seriesId;
    }

}
