package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPartAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.parseDate;

import org.atlasapi.remotesite.rovi.RoviLineParser;


public class RoviReleaseDatesLineParser implements RoviLineParser<RoviReleaseDatesLine>{

    private static final int PROGRAM_ID_POS = 0;
    private static final int RELEASE_DATE_POS = 1;
    private static final int RELEASE_COUNTRY_POS = 2;
    private static final int RELEASE_TYPE_POS = 3;
    
    @Override
    public RoviReleaseDatesLine apply(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviReleaseDatesLine.Builder builder = RoviReleaseDatesLine.builder();
        
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withReleaseCountry(getPartAtPosition(parts, RELEASE_COUNTRY_POS));
        
        String releaseDate = getPartAtPosition(parts, RELEASE_DATE_POS);
        builder.withReleaseDate(parseDate(releaseDate));
        
        builder.withReleaseType(getPartAtPosition(parts, RELEASE_TYPE_POS));
        
        return builder.build();
    }

}
