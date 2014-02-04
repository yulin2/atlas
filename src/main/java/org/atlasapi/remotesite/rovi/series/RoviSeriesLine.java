package org.atlasapi.remotesite.rovi.series;

import org.atlasapi.remotesite.rovi.KeyedLine;


public class RoviSeriesLine implements KeyedLine<String>{

    private final String seriesId;
    private final String fullTitle;
    private final String synopsis;
    
    public RoviSeriesLine(String seriesId, String fullTitle, String synopsis) {
        this.seriesId = seriesId;
        this.fullTitle = fullTitle;
        this.synopsis = synopsis;
    }
    
    public String getSeriesId() {
        return seriesId;
    }
    
    public String getFullTitle() {
        return fullTitle;
    }
    
    public String getSynopsis() {
        return synopsis;
    }

    @Override
    public String getKey() {
        return seriesId;
    }

}
