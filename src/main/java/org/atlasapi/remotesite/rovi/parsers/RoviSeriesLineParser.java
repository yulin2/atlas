package org.atlasapi.remotesite.rovi.parsers;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.parsers.RoviParsers.getActionTypeAtPosition;
import static org.atlasapi.remotesite.rovi.parsers.RoviParsers.getPartAtPosition;

import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviSeriesLine;


public class RoviSeriesLineParser implements RoviLineParser<RoviSeriesLine> {

    private final static int SERIES_ID_POS = 0;
    private final static int TITLE_POS = 1;
    private final static int SYNOPSIS_POS = 2;
    private final static int ACTION_TYPE_POS = 3;
    
    @Override
    public RoviSeriesLine apply(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        String seriesId = getPartAtPosition(parts, SERIES_ID_POS);
        String fullTitle = getPartAtPosition(parts, TITLE_POS);
        String synopsis = getPartAtPosition(parts, SYNOPSIS_POS);
        ActionType actionType = getActionTypeAtPosition(parts, ACTION_TYPE_POS);
        
        return new RoviSeriesLine(seriesId, fullTitle, synopsis, actionType);
    }
    
}
