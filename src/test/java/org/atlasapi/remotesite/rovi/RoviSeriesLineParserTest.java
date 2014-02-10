package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;

import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLineParser;
import org.junit.Test;


public class RoviSeriesLineParserTest {
    
    private final RoviSeriesLineParser parser = new RoviSeriesLineParser();
    
    @Test
    public void testParse() {
        
        String line = "13388464|Sharpe|The adventures of a British soldier in the Napoleonic Wars.|Ins";
        
        RoviSeriesLine seriesLine = parser.parseLine(line);
        
        assertEquals("13388464", seriesLine.getSeriesId());
        assertEquals("Sharpe", seriesLine.getFullTitle());
        assertEquals("The adventures of a British soldier in the Napoleonic Wars.", seriesLine.getSynopsis().get());
    }

}
