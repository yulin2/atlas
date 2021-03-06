package org.atlasapi.remotesite.rovi.parsers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.parsers.RoviProgramDescriptionLineParser;
import org.junit.Test;


public class RoviProgramDescriptionLineParserTest {

    private final RoviProgramDescriptionLineParser parser = new RoviProgramDescriptionLineParser();
    
    @Test
    public void testParseLine() {
        String line = "3037160|1234567|English - NA|Generic Description|Former friends dispute personal property.||Ins";
        
        RoviProgramDescriptionLine roviLine = parser.apply(line);
        
        assertEquals("3037160", roviLine.getProgramId());
        assertEquals("3037160", roviLine.getKey());
        assertEquals("1234567", roviLine.getSourceId().get());
        assertEquals("English - NA", roviLine.getDescriptionCulture());
        assertEquals("Generic Description", roviLine.getDescriptionType());
        assertEquals("Former friends dispute personal property.", roviLine.getDescription().get());
        assertEquals(ActionType.INSERT, roviLine.getActionType());
    }

    @Test
    public void testParseLineSourceAbsent() {
        String line = "3037160||English - NA|Generic Description|Former friends dispute personal property.||Ins";
        
        RoviProgramDescriptionLine roviLine = parser.apply(line);
        
        assertFalse(roviLine.getSourceId().isPresent());
    }
    
    @Test
    public void testParseDeleteLine() {
        String line = "22400116||German Generic|Generic Description|||Del";
        
        RoviProgramDescriptionLine roviLine = parser.apply(line);
        
        assertEquals("22400116", roviLine.getProgramId());
        assertEquals("German Generic", roviLine.getDescriptionCulture());
        assertEquals("Generic Description", roviLine.getDescriptionType());
        assertEquals(ActionType.DELETE, roviLine.getActionType());
    }
    
}
