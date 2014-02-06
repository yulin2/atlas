package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;

import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLineParser;
import org.junit.Test;


public class RoviProgramLineParserTest {
    
    private final RoviProgramLineParser parser = new RoviProgramLineParser();
    
    @Test
    public void testParseLine() {
        String line = "SM|15343105|15343105|SEASON1|VARIANT1|15343105|10760531|Y|1|NFL on Five Long Title|NFL on Five|NFL on Five|NFL on Five|NFL on F|||||NFL on Five||Sports||Episode Title|13|1500||en|N||None|None|||Color|||Ins|||1879";

        RoviProgramLine roviLine = parser.parseLine(line);
        
        assertEquals(RoviShowType.SM, roviLine.getShowType());
        assertEquals("en", roviLine.getLanguage());
        assertEquals("15343105", roviLine.getProgramId());
        assertEquals("15343105", roviLine.getSeriesId().get());
        assertEquals("SEASON1", roviLine.getSeasonId().get());
        assertEquals("15343105", roviLine.getTitleParentId().get());
        assertEquals("NFL on Five Long Title", roviLine.getLongTitle());
        assertEquals("Episode Title", roviLine.getEpisodeTitle().get());
        assertEquals("13", roviLine.getEpisodeNumber().get());
        assertEquals(1500, roviLine.getDuration().getStandardSeconds());
    }

}