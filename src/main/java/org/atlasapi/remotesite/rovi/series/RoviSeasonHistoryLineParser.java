package org.atlasapi.remotesite.rovi.series;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.RoviUtils.getActionTypeAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPartAtPosition;

import org.atlasapi.remotesite.rovi.RoviLineParser;

import com.google.common.base.Strings;


public class RoviSeasonHistoryLineParser implements RoviLineParser<RoviSeasonHistoryLine>{

    private static final int SERIES_ID_POS = 0;
    private static final int SEASON_PROGRAM_ID_POS = 1;
    private static final int SEASON_NUMBER_POS = 2;
    private static final int SEASON_NAME_POS = 3;
    private static final int SEASON_HISTORY_ID_POS = 10;
    private static final int ACTION_TYPE_POS = 9;
    
    @Override
    public RoviSeasonHistoryLine apply(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviSeasonHistoryLine.Builder builder = RoviSeasonHistoryLine.builder();
        builder.withSeriesId(getPartAtPosition(parts, SERIES_ID_POS));
        builder.withSeasonProgramId(getPartAtPosition(parts, SEASON_PROGRAM_ID_POS));
        builder.withSeasonHistoryId(getPartAtPosition(parts, SEASON_HISTORY_ID_POS));
        builder.withSeasonName(getPartAtPosition(parts, SEASON_NAME_POS));
        
        String seasonNumber = getPartAtPosition(parts, SEASON_NUMBER_POS);
        if (!Strings.isNullOrEmpty(seasonNumber)) {
            builder.withSeasonNumber(Integer.valueOf(seasonNumber));
        }
        
        builder.withActionType(getActionTypeAtPosition(parts, ACTION_TYPE_POS));
        
        return builder.build();
    }

}
