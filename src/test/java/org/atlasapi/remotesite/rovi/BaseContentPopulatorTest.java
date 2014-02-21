package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.junit.Assert.assertEquals;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.deltas.BaseContentPopulator;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class BaseContentPopulatorTest {

    private static final String PROGRAM_ID = "123456";
    
    @Mock
    private ContentResolver contentResolver;
    
    @Test
    public void testPopulation() {
        RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withShowType(RoviShowType.SERIES_MASTER);
        programLine.withProgramId(PROGRAM_ID);
        programLine.withLongTitle("This is a brand");
        programLine.withActionType(ActionType.INSERT);
        
        BaseContentPopulator<Brand> populator = new BaseContentPopulator<Brand>(
                Optional.of(programLine.build()),
                RoviTestUtils.descriptions(PROGRAM_ID),
                contentResolver);
        
        Brand brand = new Brand();
        populator.populateContent(brand);
        
        assertEquals(canonicalUriForProgram(PROGRAM_ID), brand.getCanonicalUri());
        assertEquals(RoviTestUtils.LONG_DESCRIPTION, brand.getLongDescription());
        assertEquals(RoviTestUtils.MEDIUM_DESCRIPTION, brand.getMediumDescription());
        assertEquals(RoviTestUtils.SHORT_DESCRIPTION, brand.getShortDescription());
        assertEquals(RoviTestUtils.LONG_DESCRIPTION, brand.getDescription());
    }
    
    @Test(expected=RuntimeException.class)
    public void testPopulationFromDeletionShouldThrowAnException() {
        RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withProgramId(PROGRAM_ID);
        programLine.withActionType(ActionType.DELETE);
        
        BaseContentPopulator<Brand> populator = new BaseContentPopulator<Brand>(
                Optional.of(programLine.build()),
                RoviTestUtils.descriptions(PROGRAM_ID),
                contentResolver);  
        
        populator.populateContent(new Brand());
    }
     
}
