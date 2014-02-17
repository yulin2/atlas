package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.RoviUtils.getActionTypeAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.getIntPartAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.getLongPartAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPartAtPosition;

import org.atlasapi.remotesite.rovi.RoviLineParser;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.joda.time.Duration;


public class RoviProgramLineParser implements RoviLineParser<RoviProgramLine>{
    // Positions in the line
    private static final int SHOW_TYPE_POS = 0;
    private static final int PROGRAM_ID_POS = 1;
    private static final int SERIES_ID_POS = 2;
    private static final int SEASON_ID_POS = 3;
    private static final int TITLE_PARENT_ID_POS = 5;
    private static final int LONG_TITLE_POS = 9;
    private static final int EPISODE_TITLE_POS = 22;
    private static final int EPISODE_NUMBER_POS = 23;
    private static final int DURATION_POS = 24;
    private static final int RELEASE_YEAR_POS = 25;
    private static final int LANGUAGE_POS = 26;
    private static final int ACTION_TYPE_POS = 36;
    
    @Override
    public RoviProgramLine apply(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviProgramLine.Builder builder = RoviProgramLine.builder();
        
        String showTypePart = getPartAtPosition(parts, SHOW_TYPE_POS);
        
        if (showTypePart != null) {
            builder.withShowType(RoviShowType.valueOf(showTypePart));
        }
        
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withLongTitle(getPartAtPosition(parts, LONG_TITLE_POS));
        builder.withTitleParentId(getPartAtPosition(parts, TITLE_PARENT_ID_POS));
        builder.withSeriesId(getPartAtPosition(parts, SERIES_ID_POS));
        builder.withSeasonId(getPartAtPosition(parts, SEASON_ID_POS));
        builder.withEpisodeTitle(getPartAtPosition(parts, EPISODE_TITLE_POS));
        builder.withEpisodeNumber(getPartAtPosition(parts, EPISODE_NUMBER_POS));
        
        Long durationPart = getLongPartAtPosition(parts, DURATION_POS);
        if (durationPart != null) {
            builder.withDuration(Duration.standardSeconds(durationPart));
        }
        
        builder.withLanguage(getPartAtPosition(parts, LANGUAGE_POS));
        builder.withReleaseYear(getIntPartAtPosition(parts, RELEASE_YEAR_POS));
        builder.withActionType(getActionTypeAtPosition(parts, ACTION_TYPE_POS));
        
        return builder.build();
    }
}
