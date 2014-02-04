package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.series.RoviSeasonHistoryLineParser;
import org.junit.Test;


public class RoviSeasonHistoryLineParserTest {

    private final RoviSeasonHistoryLineParser parser = new RoviSeasonHistoryLineParser();
    
    @Test
    public void testParseLine() {
        String line = "19423942|20391799|1|Season 1|2012|2012|NBC|This breezy sitcom centers on the relationship between Jessica Black (Jessica St. Clair) and her best friend, Lennon White (Lennon Parham).||Ins|90379";
    
        RoviSeasonHistoryLine roviLine = parser.parseLine(line);
        
        assertEquals("19423942", roviLine.getSeriesId());
        assertEquals("20391799", roviLine.getSeasonProgramId());
        assertTrue(roviLine.getSeasonNumber().isPresent());
        assertEquals(1, roviLine.getSeasonNumber().get().intValue());
        assertTrue(roviLine.getSeasonName().isPresent());
        assertEquals("Season 1", roviLine.getSeasonName().get());
    }
    
}
