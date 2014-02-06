package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;


public class RoviUtilsTest {

    @Test
    public void testParseDateWithYearOnly() {
        LocalDate date = RoviUtils.parseDate("20100000");
        
        assertEquals(2010, date.getYear());
        assertEquals(01, date.getMonthOfYear());
        assertEquals(01, date.getDayOfMonth());
    }

    @Test
    public void testParseDateWithYearAndMonth() {
        LocalDate date = RoviUtils.parseDate("20101200");
        
        assertEquals(2010, date.getYear());
        assertEquals(12, date.getMonthOfYear());
        assertEquals(01, date.getDayOfMonth());
    }

    @Test
    public void testParseFullDate() {
        LocalDate date = RoviUtils.parseDate("20101223");
        
        assertEquals(2010, date.getYear());
        assertEquals(12, date.getMonthOfYear());
        assertEquals(23, date.getDayOfMonth());
    }
    
}
