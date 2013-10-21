package org.atlasapi.remotesite.talktalk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.base.Optional;


public class TalkTalkFilmYearExtractorTest {

    private final TalkTalkFilmYearExtractor extractor = new TalkTalkFilmYearExtractor();
    
    @Test
    public void testExtractsYear() {
        assertEquals(extractor.extractYear("1960s East End geezers Ray Winstone and Jack Huston stumble across a lump of uranium and set off on a European adventure to flog it to the highest bidder. Crime comedy. (2011)(96mins)"), Optional.of(2011));
    }
   
    @Test
    public void testDoesntExtractYearIfMissing() {
        assertEquals(extractor.extractYear("1960s East End geezers Ray Winstone and Jack Huston stumble across a lump of uranium and set off on a European adventure to flog it to the highest bidder. Crime comedy."), Optional.absent());
    }
    
}
