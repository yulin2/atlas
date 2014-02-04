package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLineParser;
import org.junit.Test;


public class RoviProgramDescriptionLineParserTest {

    private final RoviProgramDescriptionLineParser parser = new RoviProgramDescriptionLineParser();
    
    @Test
    public void testParseLine() {
        String line = "3037160|1234567|English - NA|Generic Description|Former friends dispute personal property.||Ins";
        
        RoviProgramDescriptionLine roviLine = parser.parseLine(line);
        
        assertEquals("3037160", roviLine.getProgramId());
        assertEquals("3037160", roviLine.getKey());
        assertEquals("1234567", roviLine.getSourceId().get());
        assertEquals("English - NA", roviLine.getDescriptionCulture());
        assertEquals("Generic Description", roviLine.getDescriptionType());
        assertEquals("Former friends dispute personal property.", roviLine.getDescription());
    }

    @Test
    public void testParseLineSourceAbsent() {
        String line = "3037160||English - NA|Generic Description|Former friends dispute personal property.||Ins";
        
        RoviProgramDescriptionLine roviLine = parser.parseLine(line);
        
        assertFalse(roviLine.getSourceId().isPresent());
    }
    
}
