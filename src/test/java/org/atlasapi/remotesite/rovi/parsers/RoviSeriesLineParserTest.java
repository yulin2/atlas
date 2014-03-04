package org.atlasapi.remotesite.rovi.parsers;


import static org.junit.Assert.assertEquals;

import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.parsers.RoviSeriesLineParser;
import org.junit.Test;


public class RoviSeriesLineParserTest {
    
    private final RoviSeriesLineParser parser = new RoviSeriesLineParser();
    
    @Test
    public void testParse() {
        
        String line = "13388464|Sharpe|The adventures of a British soldier in the Napoleonic Wars.|Ins";
        
        RoviSeriesLine seriesLine = parser.apply(line);
        
        assertEquals("13388464", seriesLine.getSeriesId());
        assertEquals("Sharpe", seriesLine.getFullTitle().get());
        assertEquals("The adventures of a British soldier in the Napoleonic Wars.", seriesLine.getSynopsis().get());
        assertEquals(ActionType.INSERT, seriesLine.getActionType());
    }

    @Test
    public void testParseDeleteLine() {
        
        String line = "20949148|Amy Winehouse - Live at Shepherds Bush Empire||Del";
        
        RoviSeriesLine seriesLine = parser.apply(line);
        
        assertEquals("20949148", seriesLine.getSeriesId());
        assertEquals(ActionType.DELETE, seriesLine.getActionType());
    }

}
