package org.atlasapi.remotesite.rovi.series;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPartAtPosition;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.remotesite.rovi.RoviLineParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;


public class RoviTheatricalReleaseDatesLineParser implements RoviLineParser<RoviTheatricalReleaseDatesLine>{

    private static final int PROGRAM_ID_POS = 0;
    private static final int RELEASE_DATE_POS = 1;
    private static final int RELEASE_COUNTRY_POS = 2;
    private static final int RELEASE_TYPE_POS = 3;
    
    @Override
    public RoviTheatricalReleaseDatesLine parseLine(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviTheatricalReleaseDatesLine.Builder builder = RoviTheatricalReleaseDatesLine.builder();
        
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withReleaseCountry(getPartAtPosition(parts, RELEASE_COUNTRY_POS));
        
        String releaseDate = getPartAtPosition(parts, RELEASE_DATE_POS);
        builder.withReleaseDate(DateTime.parse(releaseDate, DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC()));
        
        String releaseType = getPartAtPosition(parts, RELEASE_TYPE_POS);
        if (StringUtils.isNotBlank(releaseType) && StringUtils.isNotEmpty(releaseType)) {
            builder.withReleaseType(releaseType);
        }
        
        return builder.build();
    }

}
