package org.atlasapi.remotesite.rovi.schedule;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;

import java.util.Iterator;

import org.atlasapi.remotesite.rovi.ActionType;
import org.atlasapi.remotesite.rovi.RoviLineParser;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.google.common.base.Strings;


public class ScheduleLineParser implements RoviLineParser<ScheduleLine>{

    private static DateTimeFormatter YYYYMMDD_FORMAT = new DateTimeFormatterBuilder()
                                                               .appendYear(4, 4)
                                                               .appendMonthOfYear(2)
                                                               .appendDayOfMonth(2)
                                                               .toFormatter();
    
    @Override
    public ScheduleLine apply(String line) {
        Iterator<String> lineIt = LINE_SPLITTER.split(line).iterator();
        
        String sourceId = parseAsString(lineIt.next());
        LocalDate startDate = parseAsLocalDate(lineIt.next());
        LocalTime startTime = parseAsLocalTime(lineIt.next());
        Boolean isTimeApproximate = parseAsBoolean(lineIt.next());
        Integer duration = parseAsInteger(lineIt.next());
        String programmeId = parseAsString(lineIt.next());
        String seriesId = parseAsString(lineIt.next());
        String tvRating = parseAsString(lineIt.next());
        String tvRatingReason = parseAsString(lineIt.next());
        ShowingType showingType = parseAsShowingType(lineIt.next());
        CaptionType captionType = parseAsCaptionType(lineIt.next());
        AudioLevel audioLevel = parseAsAudioLevel(lineIt.next());
        String threeDLevel = parseAsString(lineIt.next());
        Boolean sap = parseAsBoolean(lineIt.next());
        String colorType = parseAsString(lineIt.next());
        String airingType = parseAsString(lineIt.next());
        Boolean subtitled = parseAsBoolean(lineIt.next());
        Boolean joinedInProgress = parseAsBoolean(lineIt.next());
        Boolean subjectToBlackout = parseAsBoolean(lineIt.next());
        String aspectRatio = parseAsString(lineIt.next());
        Boolean descriptiveVideoService = parseAsBoolean(lineIt.next());
        Integer partNumber = parseAsInteger(lineIt.next());
        Integer totalNumberOfParts = parseAsInteger(lineIt.next());
        String hdtvLevel = parseAsString(lineIt.next());
        Boolean syndicated = parseAsBoolean(lineIt.next());
        String delta = parseAsString(lineIt.next());
        String scheduleId = parseAsString(lineIt.next());
                
        return new ScheduleLine(sourceId, startDate, startTime, isTimeApproximate, duration, 
                programmeId, seriesId, tvRating, tvRatingReason, showingType, captionType, 
                audioLevel, threeDLevel, sap, colorType, airingType, subtitled, joinedInProgress, 
                subjectToBlackout, aspectRatio, descriptiveVideoService, partNumber, 
                totalNumberOfParts, hdtvLevel, syndicated, ActionType.fromRoviType(delta), scheduleId);
    }
    
    private AudioLevel parseAsAudioLevel(String s) {
        // TODO Auto-generated method stub
        return null;
    }

    private CaptionType parseAsCaptionType(String s) {
        // TODO
        return null;
    }

    private ShowingType parseAsShowingType(String s) {
        // TODO
        return null;
    }

    private String parseAsString(String s) {
        return Strings.emptyToNull(s);
    }
    private Boolean parseAsBoolean(String s) {
        if (Strings.emptyToNull(s) == null) {
            return null;
        }
        if ("Y".equals(s)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private LocalTime parseAsLocalTime(String s) {
        if (Strings.emptyToNull(s) == null) {
            return null;
        }
        return LocalTime.parse(s);
    }

    private LocalDate parseAsLocalDate(String s) {
        if (Strings.emptyToNull(s) == null) {
            return null;
            
        }
        return LocalDate.parse(s, YYYYMMDD_FORMAT);
    }

    private Integer parseAsInteger(String s) {
        if (Strings.emptyToNull(s) == null) {
            return null;
        }
        return Integer.parseInt(s);
    }

}
