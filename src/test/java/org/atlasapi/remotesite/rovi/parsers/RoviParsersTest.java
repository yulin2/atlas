package org.atlasapi.remotesite.rovi.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.joda.time.DateTimeFieldType;
import org.joda.time.ReadablePartial;
import org.junit.Test;


public class RoviParsersTest {

    @Test
    public void testParseDateWithYearOnly() {
        ReadablePartial date = RoviParsers.parsePotentiallyPartialDate("20100000");
        
        assertEquals(2010, date.get(DateTimeFieldType.year()));
        assertFalse(date.isSupported(DateTimeFieldType.monthOfYear()));
        assertFalse(date.isSupported(DateTimeFieldType.dayOfMonth()));
    }

    @Test
    public void testParseDateWithYearAndMonth() {
        ReadablePartial date = RoviParsers.parsePotentiallyPartialDate("20101200");
        
        assertEquals(2010, date.get(DateTimeFieldType.year()));
        assertEquals(12, date.get(DateTimeFieldType.monthOfYear()));
        assertFalse(date.isSupported(DateTimeFieldType.dayOfMonth()));
    }

    @Test
    public void testParseFullDate() {
        ReadablePartial date = RoviParsers.parsePotentiallyPartialDate("20101223");
        
        assertEquals(2010, date.get(DateTimeFieldType.year()));
        assertEquals(12, date.get(DateTimeFieldType.monthOfYear()));
        assertEquals(23, date.get(DateTimeFieldType.dayOfMonth()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIncorrectDate() {
        RoviParsers.parsePotentiallyPartialDate("2010");
    }
    
}
