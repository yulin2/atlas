package org.atlasapi.remotesite.rovi.parsers;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.parsers.RoviParsers.getPartAtPosition;
import static org.atlasapi.remotesite.rovi.parsers.RoviParsers.parsePotentiallyPartialDate;

import org.atlasapi.remotesite.rovi.model.RoviReleaseDatesLine;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;


public class RoviReleaseDatesLineParser implements RoviLineParser<RoviReleaseDatesLine>{

    private static final int PROGRAM_ID_POS = 0;
    private static final int RELEASE_DATE_POS = 1;
    private static final int RELEASE_COUNTRY_POS = 2;
    private static final int RELEASE_TYPE_POS = 3;
    
    private static final LocalDate BASE_LOCAL_DATE = new LocalDate().withMonthOfYear(01).withDayOfMonth(01);
    
    @Override
    public RoviReleaseDatesLine apply(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviReleaseDatesLine.Builder builder = RoviReleaseDatesLine.builder();
        
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withReleaseCountry(getPartAtPosition(parts, RELEASE_COUNTRY_POS));
        
        String releaseDate = getPartAtPosition(parts, RELEASE_DATE_POS);
        ReadablePartial partialDate = parsePotentiallyPartialDate(releaseDate);
        builder.withReleaseDate(partialDateToLocalDate(partialDate));
        
        builder.withReleaseType(getPartAtPosition(parts, RELEASE_TYPE_POS));
        
        return builder.build();
    }
    
    private LocalDate partialDateToLocalDate(ReadablePartial partialDate) {
        LocalDate localDate = BASE_LOCAL_DATE;
        
        if (partialDate.isSupported(DateTimeFieldType.year())) {
            localDate = localDate.withYear(partialDate.get(DateTimeFieldType.year()));
        }

        if (partialDate.isSupported(DateTimeFieldType.monthOfYear())) {
            localDate = localDate.withMonthOfYear(partialDate.get(DateTimeFieldType.monthOfYear()));
        }
        
        if (partialDate.isSupported(DateTimeFieldType.dayOfMonth())) {
            localDate = localDate.withDayOfMonth(partialDate.get(DateTimeFieldType.dayOfMonth()));
        }
        
        return localDate;
    }

}
