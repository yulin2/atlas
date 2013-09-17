package org.atlasapi.remotesite.bbc.nitro;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.atlasapi.remotesite.bbc.nitro.AroundTodayDayRangeSupplier.Builder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.collect.Range;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.TimeMachine;


public class AroundTodayDayRangeSupplierTest {

    private final DateTime now = new DateTime(DateTimeZones.UTC);
    private final LocalDate today = now.toLocalDate();
    private final TimeMachine clock = new TimeMachine(now);
    
    @Test
    public void testRangeWithZeroBackAndForward() {
        Range<LocalDate> closedClosed = builder()
            .withDaysBack(0)
            .withDaysForward(0)
            .build()
            .get();
        assertFalse(closedClosed.isEmpty());
        assertTrue(closedClosed.contains(today));
        assertFalse(closedClosed.contains(today.plusDays(1)));
        assertFalse(closedClosed.contains(today.minusDays(1)));
    }

    @Test
    public void testRangeWithSevenBackAndForward() {
        Range<LocalDate> closedClosed = builder()
                .withDaysBack(7)
                .withDaysForward(7)
                .build()
                .get();
        assertFalse(closedClosed.isEmpty());
        assertTrue(closedClosed.contains(today));
        assertTrue(closedClosed.contains(today.minusDays(7)));
        assertFalse(closedClosed.contains(today.minusDays(8)));
        assertTrue(closedClosed.contains(today.plusDays(7)));
        assertFalse(closedClosed.contains(today.plusDays(8)));
    }

    private Builder builder() {
        return AroundTodayDayRangeSupplier.builder()
            .withClock(clock);
    }

}
