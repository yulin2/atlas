package org.atlasapi.remotesite.rovi.model;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.rovi.indexing.KeyedActionLine;

import com.google.common.base.Optional;

public class RoviSeriesLine implements KeyedActionLine<String>{

    private final String seriesId;
    private final ActionType actionType;

    private final Optional<String> fullTitle;
    private final Optional<String> synopsis;
    
    public RoviSeriesLine(String seriesId, String fullTitle, String synopsis, ActionType actionType) {
        this.seriesId = checkNotNull(seriesId);
        this.actionType = checkNotNull(actionType);

        this.fullTitle = Optional.fromNullable(fullTitle);
        this.synopsis = Optional.fromNullable(synopsis);
    }
    
    public String getSeriesId() {
        return seriesId;
    }
    
    public Optional<String> getFullTitle() {
        return fullTitle;
    }
    
    public Optional<String> getSynopsis() {
        return synopsis;
    }

    @Override
    public String getKey() {
        return seriesId;
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }

}
