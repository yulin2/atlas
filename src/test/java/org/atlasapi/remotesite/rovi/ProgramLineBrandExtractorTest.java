package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_INSERT;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.program.ProgramLineBrandExtractor;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ProgramLineBrandExtractorTest {
    
    private static final String SHORT_DESCRIPTION = "This is the short description";
    private static final String MEDIUM_DESCRIPTION = "This is the medium description";
    private static final String LONG_DESCRIPTION = "This is the long description";
    private static final String PROGRAM_ID = "12345";
    
    @Mock
    private KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    @Mock
    private ContentResolver contentResolver;
    
    private ProgramLineBrandExtractor extractor;
    
    @Before
    public void init() throws IOException, IndexAccessException {
        when(descriptionIndex.getLinesForKey(PROGRAM_ID, IS_INSERT)).thenReturn(RoviTestUtils.descriptions(PROGRAM_ID));
        extractor = new ProgramLineBrandExtractor(descriptionIndex, contentResolver);
    }
    

    @Test
    public void testExtractContent() throws IndexAccessException {
        RoviProgramLine.Builder programLine = RoviProgramLine.builder();
        
        programLine.withShowType(RoviShowType.SERIES_MASTER);
        programLine.withProgramId(PROGRAM_ID);
        programLine.withLongTitle("This is a brand");
        programLine.withActionType(ActionType.INSERT);
        
        Brand brand = extractor.extract(programLine.build());
        
        assertEquals(canonicalUriForProgram(PROGRAM_ID), brand.getCanonicalUri());
        assertEquals(LONG_DESCRIPTION, brand.getLongDescription());
        assertEquals(MEDIUM_DESCRIPTION, brand.getMediumDescription());
        assertEquals(SHORT_DESCRIPTION, brand.getShortDescription());
        assertEquals(LONG_DESCRIPTION, brand.getDescription());
    }

}
