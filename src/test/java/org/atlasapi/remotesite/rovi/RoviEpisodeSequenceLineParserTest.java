package org.atlasapi.remotesite.rovi;


import static org.junit.Assert.assertEquals;

import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLineParser;
import org.junit.Test;


public class RoviEpisodeSequenceLineParserTest {

    private final RoviEpisodeSequenceLineParser parser = new RoviEpisodeSequenceLineParser();
    
    @Test
    public void testParseLine() {
        String line = "3757920|13376601|3832987|Amazon Redux|6|10|10|Ins";
        
        RoviEpisodeSequenceLine roviLine = parser.apply(line);
        
        assertEquals("3757920", roviLine.getSeriesId().get());
        assertEquals("13376601", roviLine.getSeasonProgramId().get());
        assertEquals("3832987", roviLine.getProgramId());
        assertEquals("Amazon Redux", roviLine.getEpisodeTitle().get());
        assertEquals(10, roviLine.getEpisodeSeasonSequence().get().intValue());
        assertEquals(6, roviLine.getEpisodeSeasonNumber().get().intValue());
        assertEquals(ActionType.INSERT, roviLine.getActionType());
    }
    
    @Test
    public void testDeleteLine() {
        String line = "19939266|19953260|19937257|||||Del";
        
        RoviEpisodeSequenceLine roviLine = parser.apply(line);
        
        assertEquals("19937257", roviLine.getProgramId());
        assertEquals(ActionType.DELETE, roviLine.getActionType());
    }
    
}
