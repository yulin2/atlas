package org.atlasapi.remotesite.pa;

import junit.framework.TestCase;

import org.joda.time.DateTimeZone;

public class PaUpdaterTest extends TestCase {

    public void testShouldGetCorrectBizzarePaDateTimes() {
        DateTimeZone zone26 = PaUpdater.getTimeZone("20100326");
        DateTimeZone zone27 = PaUpdater.getTimeZone("20100327");
        DateTimeZone zone28 = PaUpdater.getTimeZone("20100328");
        
        assertEquals(DateTimeZone.forOffsetHours(0), PaProgrammeProcessor.getTransmissionTime("26/03/2010", "11:00", zone26).getZone());
        assertEquals(DateTimeZone.forOffsetHours(0), PaProgrammeProcessor.getTransmissionTime("27/03/2010", "11:00", zone27).getZone());
        assertEquals(DateTimeZone.forOffsetHours(0), PaProgrammeProcessor.getTransmissionTime("28/03/2010", "1:00", zone27).getZone());
        assertEquals(DateTimeZone.forOffsetHours(1), PaProgrammeProcessor.getTransmissionTime("28/03/2010", "11:00", zone28).getZone());
        
        assertEquals(DateTimeZone.forOffsetHours(0), zone26);
        assertEquals(DateTimeZone.forOffsetHours(0), zone27);
        assertEquals(DateTimeZone.forOffsetHours(1), zone28);
    }
}
