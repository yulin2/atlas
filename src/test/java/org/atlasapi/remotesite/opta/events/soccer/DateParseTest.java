package org.atlasapi.remotesite.opta.events.soccer;

import static org.junit.Assert.*;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;


public class DateParseTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    
    @Test
    public void testParseOfDate() {
        String dateStr = "2014-08-16 17:30:00";
        String timeZoneStr = "BST";
        
        TimeZone javaTimeZone = TimeZone.getTimeZone(timeZoneStr);
        DateTimeZone timeZone = DateTimeZone.forTimeZone(javaTimeZone);
        DateTime parsed = DATE_TIME_FORMATTER.withZone(timeZone)
            .parseDateTime(dateStr);
        
        assertEquals(new DateTime(2014, 8, 16, 16, 30).withZone(DateTimeZone.UTC), parsed);
    }

}
