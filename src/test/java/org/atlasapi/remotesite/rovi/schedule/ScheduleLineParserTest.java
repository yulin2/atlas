package org.atlasapi.remotesite.rovi.schedule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;


public class ScheduleLineParserTest {

    private final ScheduleLineParser scheduleLineParser = new ScheduleLineParser();
    
    @Test
    public void testParse() {
        String testLine = "30863|20140118|04:55|N|1500|12291654||FSK: 12|||None|Dolby 5.1||Y|Color||N|N|N|4:3 Fullscreen|N||||N|Ins|1774482180";
        ScheduleLine parsedLine = scheduleLineParser.parseLine(testLine);
        
        assertThat(parsedLine.getSourceId(), is("30863"));
        assertThat(parsedLine.getStartDate(), is(new LocalDate(2014, DateTimeConstants.JANUARY, 18)));
        assertThat(parsedLine.getStartTime(), is(new LocalTime(4, 55)));
        assertThat(parsedLine.getDuration(), is(1500));
        
    }
}
