package org.atlasapi.remotesite.rovi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.atlasapi.remotesite.rovi.model.ActionType;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;
import org.junit.Test;


public class RoviPredicatesTest {

    @Test
    public void testIsInsertPredicateTrue() {
        RoviProgramLine.Builder line = RoviProgramLine.builder();
        line.withProgramId("12345");
        line.withActionType(ActionType.INSERT);
        
        assertTrue(RoviPredicates.IS_INSERT.apply(line.build()));
    }
    
    @Test
    public void testIsInsertPredicateFalse() {
        RoviProgramLine.Builder line = RoviProgramLine.builder();
        line.withProgramId("12345");
        line.withActionType(ActionType.DELETE);
        
        assertFalse(RoviPredicates.IS_INSERT.apply(line.build()));
    }
    
}
