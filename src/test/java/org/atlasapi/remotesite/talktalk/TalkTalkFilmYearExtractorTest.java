package org.atlasapi.remotesite.talktalk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.base.Optional;


public class TalkTalkFilmYearExtractorTest {

    private final TalkTalkFilmYearExtractor extractor = new TalkTalkFilmYearExtractor();
    
    @Test
    public void testExtractsYear() {
        assertEquals(Optional.of(2011), extractor.extractYear("1960s East End geezers Ray Winstone and Jack Huston stumble across a lump of uranium and set off on a European adventure to flog it to the highest bidder. Crime comedy. (2011)(96mins)"));
    }
   
    @Test
    public void testDoesntExtractYearIfMissing() {
        assertEquals(Optional.absent(), extractor.extractYear("1960s East End geezers Ray Winstone and Jack Huston stumble across a lump of uranium and set off on a European adventure to flog it to the highest bidder. Crime comedy."));
    }
    
    @Test
    public void testWhenDurationDoesntHaveSpaces() {
        assertEquals(Optional.of(2012), extractor.extractYear("Pilot Denzel Washington crash lands a plane, saving almost everyone on board. But in the aftermath, questions arise as to what really happened. Drama from Robert Zemeckis. (2012)(138 mins) (LANG, SEX, MATURE, FLASH)"));
    }
    
}
