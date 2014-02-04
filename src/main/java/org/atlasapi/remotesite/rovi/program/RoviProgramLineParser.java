package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.remotesite.rovi.RoviLineParser;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.joda.time.Duration;

import com.google.common.collect.Iterables;


public class RoviProgramLineParser implements RoviLineParser<RoviProgramLine>{
    // Positions in the line
    private static final int SHOW_TYPE_POS = 0;
    private static final int PROGRAM_ID_POS = 1;
    private static final int SERIES_ID_POS = 2;
    private static final int SEASON_ID_POS = 3;
    private static final int VARIANT_PARENT_ID_POS = 4;
    private static final int GROUP_ID_POS = 6;
    private static final int GROUP_LANGUAGE_PRIMARY_POS = 7;
    private static final int LONG_TITLE_POS = 9;
    private static final int EPISODE_TITLE_POS = 22;
    private static final int EPISODE_NUMBER_POS = 23;
    private static final int DURATION_POS = 24;
    private static final int LANGUAGE_POS = 26;
    
    @Override
    public RoviProgramLine parseLine(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviProgramLine.Builder builder = new RoviProgramLine.Builder();
        
        builder.withShowType(RoviShowType.valueOf(getPartAtPosition(parts, SHOW_TYPE_POS)));
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withSeriesId(getPartAtPosition(parts, SERIES_ID_POS));
        builder.withSeasonId(getPartAtPosition(parts, SEASON_ID_POS));
        builder.withVariantParentId(getPartAtPosition(parts, VARIANT_PARENT_ID_POS));
        builder.withGroupId(getPartAtPosition(parts, GROUP_ID_POS));
        builder.withIsGroupLanguagePrimary(booleanFromYesNoChar(getPartAtPosition(parts, GROUP_LANGUAGE_PRIMARY_POS)));
        builder.withLongTitle(getPartAtPosition(parts, LONG_TITLE_POS));
        builder.withEpisodeTitle(getPartAtPosition(parts, EPISODE_TITLE_POS));
        
        String episodeNumber = getPartAtPosition(parts, EPISODE_NUMBER_POS);
        
        if (StringUtils.isNotBlank(episodeNumber) && StringUtils.isNumeric(episodeNumber)) {
            builder.withEpisodeNumber(Long.valueOf(episodeNumber));
        }
        
        builder.withDuration(Duration.standardSeconds(getLongPartAtPosition(parts, DURATION_POS)));
        builder.withLanguage(getPartAtPosition(parts, LANGUAGE_POS));
        
        return builder.build();
    }
    
    private boolean booleanFromYesNoChar(String yesNoChar) {
        if (yesNoChar.equalsIgnoreCase("Y")) {
            return true;
        }
        
        return false;
    }

    private String getPartAtPosition(Iterable<String> parts, int position) {
        return Iterables.get(parts, position);
    }

    private Long getLongPartAtPosition(Iterable<String> parts, int position) {
        return Long.valueOf(getPartAtPosition(parts, position));
    }
}
