package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;

import org.atlasapi.remotesite.rovi.program.RoviReleaseDatesLine;
import org.atlasapi.remotesite.rovi.program.RoviReleaseDatesLineParser;
import org.junit.Test;

public class RoviReleaseDatesLineParserTest {

    private final RoviReleaseDatesLineParser parser = new RoviReleaseDatesLineParser();
    
    @Test
    public void testParseLine() {
        String line = "20210035|20121004|AT|Wide|Ins|204168";
        
        RoviReleaseDatesLine roviLine = parser.parseLine(line);
        
        assertEquals("20210035", roviLine.getProgramId());
        assertEquals("20121004", roviLine.getReleaseDate().toString("yyyyMMdd"));
        assertEquals("AT", roviLine.getReleaseCountry());
        assertEquals("Wide", roviLine.getReleaseType().get());
    }
}
